package com.mineinabyss.armorstandlock

import com.mineinabyss.components.armorstandlock.LockArmorStand
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.minecraft.access.toGeary
import com.mineinabyss.geary.minecraft.store.encodeComponentsTo
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
                    "toggle"(desc = "Toggles if an armor stand should be protected or not") {
                        playerAction {
                            val entity = player.playerData.recentRightclickedEntity ?: return@playerAction
                            val locked = entity.toGeary().get<LockArmorStand>() ?: return@playerAction

                            locked.lockState = !locked.lockState
                            entity.toGeary().encodeComponentsTo(entity)
                            if (locked.lockState) player.success("This armor stand is now protected!")
                            else player.error("This armor stand is no longer protected!")
                        }
                    }
                    "add"(desc = "Add a player to this armor stand.") {
                        val playerName by stringArg()
                        playerAction {
                            val entity = player.playerData.recentRightclickedEntity ?: return@playerAction
                            val locked = entity.toGeary().get<LockArmorStand>() ?: return@playerAction
                            val uuid = Bukkit.getOfflinePlayer(playerName).uniqueId

                            if (locked.isAllowed(uuid)) {
                                player.error("This player can already interact with this armor stand")
                                return@playerAction
                            }
                            else {
                                locked.allowedAccess.add(uuid)
                                entity.toGeary().encodeComponentsTo(entity)
                                player.success("$playerName can now interact with this armor stand")
                            }
                        }
                    }

                    "remove"(desc = "Remove a player to this armor stand.") {
                        val playerName by stringArg {
                            parseErrorMessage = { "No player with name: $passed." }
                        }
                        playerAction {
                            val entity = player.playerData.recentRightclickedEntity ?: return@playerAction
                            val locked = entity.toGeary().get<LockArmorStand>() ?: return@playerAction
                            val uuid = Bukkit.getOfflinePlayer(playerName).uniqueId

                            if (locked.isAllowed(uuid)) {
                                locked.allowedAccess.remove(uuid)
                                entity.toGeary().encodeComponentsTo(entity)
                                player.success("$playerName has been removed from this armor stand")
                                return@playerAction
                            }
                            player.error("This player cannot interact with this armor stand")
                        }
                    }

                    "clear"(desc = "Clear all other players from this armor stand.") {
                        playerAction {
                            val entity = player.playerData.recentRightclickedEntity ?: return@playerAction
                            val locked = entity.toGeary().get<LockArmorStand>() ?: return@playerAction

                            locked.allowedAccess.clear()
                            locked.allowedAccess.add(locked.owner)
                            entity.toGeary().encodeComponentsTo(entity)
                            player.success("All players were removed from this armor stand")
                        }
                    }

                    "check" (desc = "Get a list of all players allowed to interact with this armor stand.") {
                        playerAction {
                            val entity = player.playerData.recentRightclickedEntity ?: return@playerAction
                            val locked = entity.toGeary().get<LockArmorStand>() ?: return@playerAction

                            if (locked.lockState) {
                                player.success("These people can interact with your armor stand:")
                                locked.allowedAccess.forEach {
                                    player.info(it?.toPlayer()?.name)
                                }
                            }
                            else {
                                player.error("This armor stand is not protected.")
                                player.error("Anyone can interact with it.")
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
                            "lock" -> listOf("add", "remove", "clear", "check", "toggle")
                            else -> null

                        }
                    }
                    3 -> when (args[1]) {
                        "add" -> Bukkit.getOnlinePlayers().map { it.name }
                        "remove" -> Bukkit.getOnlinePlayers().map { it.name }
                        else -> null
                    }
                    else -> null
                }
            }
        }
    }
}