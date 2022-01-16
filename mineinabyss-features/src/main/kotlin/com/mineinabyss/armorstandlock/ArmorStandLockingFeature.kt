package com.mineinabyss.armorstandlock

import com.mineinabyss.components.armorstandlock.LockArmorStand
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.minecraft.access.toGeary
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit

@Serializable
@SerialName("lock_armorstand")
class ArmorStandLockingFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(ArmorStandLockingListener())

        commands {
            mineinabyss {
                "lock"(desc = "Protection related commands") {
                    "add"(desc = "Add a player to this armor stand.") {
                        val playerName by stringArg()
                        playerAction {
                            val entity = player.playerData.recentRightclickedEntity
                            val locked = entity?.toGeary()?.get<LockArmorStand>() ?: return@playerAction
                            val uuid = Bukkit.getOfflinePlayer(playerName).uniqueId

                            if (locked.allowedAccess.contains(uuid)) {
                                player.error("This player can already interact with this armor stand")
                                return@playerAction
                            }
                            else {
                                locked.allowedAccess += uuid
                                player.success("$playerName can now interact with this armor stand")
                            }
                        }
                    }

                    "remove"(desc = "Remove a player to this armor stand.") {
                        val playerName by stringArg {
                            parseErrorMessage = { "No player with name: $passed." }
                        }
                        playerAction {
                            val entity = player.playerData.recentRightclickedEntity
                            val locked = entity?.toGeary()?.get<LockArmorStand>() ?: return@playerAction
                            val uuid = Bukkit.getOfflinePlayer(playerName).uniqueId

                            if (locked.allowedAccess.contains(uuid)) {
                                locked.allowedAccess -= uuid
                                player.success("$playerName has been removed from this armor stand")
                                return@playerAction
                            }
                            player.error("This player cannot interact with this armor stand")
                        }
                    }

                    "clear"(desc = "Clear all other players from this armor stand.") {
                        playerAction {
                            val entity = player.playerData.recentRightclickedEntity
                            val locked = entity?.toGeary()?.get<LockArmorStand>() ?: return@playerAction
                            val owner = locked.owner

                            locked.allowedAccess.forEach {
                                if (it != owner) locked.allowedAccess.minus(it)
                                player.success("All players were removed from this armor stand")
                            }
                        }
                    }

                    "check" (desc = "Get a list of all players allowed to interact with this armor stand.") {
                        playerAction {
                            val entity = player.playerData.recentRightclickedEntity
                            val locked = entity?.toGeary()?.get<LockArmorStand>() ?: return@playerAction

                            player.success("These people can interact with your armor stand:")
                            locked.allowedAccess.forEach {
                                player.info(it?.toPlayer()?.name)
                            }
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf(
                        "lock"
                    ).filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "lock" -> listOf("add", "remove", "clear", "check")
                            else -> null

                        }
                    }
                    else -> null
                }
            }
        }
    }
}