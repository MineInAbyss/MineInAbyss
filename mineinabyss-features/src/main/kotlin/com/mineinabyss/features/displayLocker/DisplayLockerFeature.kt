package com.mineinabyss.features.displayLocker

import com.mineinabyss.components.displaylocker.LockDisplayItem
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import org.bukkit.Bukkit
import org.bukkit.entity.Player


@kotlinx.serialization.Serializable
@SerialName("lock_display_item")
class DisplayLockerFeature(
    val bypassPermission: String = "mineinabyss.lockdisplay.bypass",
) : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        listeners(DisplayLockerListener(this@DisplayLockerFeature))

        commands {
            mineinabyss {
                "lock"(desc = "Protection related commands") {
                    "default_state"(desc= "Toggles the default lockstate of player") {
                        playerAction {
                            player.playerData.defaultDisplayLockState = !player.playerData.defaultDisplayLockState
                            player.success("Your default lockstate is now ${if (player.playerData.defaultDisplayLockState) "locked" else "unlocked"}")
                        }
                    }
                    "toggle"(desc = "Toggles if a display item should be protected or not") {
                        playerAction {
                            val player = sender as Player
                            val entity = player.playerData.getRecentEntity() ?: return@playerAction
                            val locked = entity.toGeary().get<LockDisplayItem>() ?: return@playerAction
                            locked.lockState = !locked.lockState
                            entity.setGravity(!locked.lockState)

                            if (locked.lockState) player.success("This ${entity.name} is now protected!")
                            else player.error("This ${entity.name} is no longer protected!")
                        }
                    }
                    "add"(desc = "Add a player to this display item.") {
                        val playerName by stringArg()
                        playerAction {
                            val player = sender as Player
                            val entity = player.playerData.getRecentEntity() ?: return@playerAction
                            val locked = entity.toGeary().get<LockDisplayItem>() ?: return@playerAction
                            val uuid = Bukkit.getOfflinePlayer(playerName).uniqueId

                            if (uuid in locked.allowedAccess) {
                                player.error("$playerName can already interact with this ${entity.name}")
                                return@playerAction
                            } else {
                                locked.allowedAccess.add(uuid)
                                entity.toGeary().encodeComponentsTo(entity)
                                player.success("$playerName can now interact with this ${entity.name}")
                            }
                        }
                    }

                    "remove"(desc = "Remove a player to this display item.") {
                        val playerName by stringArg {
                            parseErrorMessage = { "No player with name: $passed." }
                        }
                        playerAction {
                            val player = sender as Player
                            val entity = player.playerData.getRecentEntity() ?: return@playerAction
                            val locked = entity.toGeary().get<LockDisplayItem>() ?: return@playerAction
                            val uuid = Bukkit.getOfflinePlayer(playerName).uniqueId

                            if (uuid in locked.allowedAccess) {
                                locked.allowedAccess.remove(uuid)
                                entity.toGeary().encodeComponentsTo(entity)
                                player.success("$playerName has been removed from this ${entity.name}")
                                return@playerAction
                            }
                            player.error("$playerName cannot interact with this ${entity.name}")
                        }
                    }

                    "clear"(desc = "Clear all other players from this display item") {
                        playerAction {
                            val player = sender as Player
                            val entity = player.playerData.getRecentEntity() ?: return@playerAction
                            val locked = entity.toGeary().get<LockDisplayItem>() ?: return@playerAction

                            locked.allowedAccess.clear()
                            locked.allowedAccess.add(locked.owner)
                            entity.toGeary().encodeComponentsTo(entity)
                            player.success("All players were removed from this ${entity.name}")
                        }
                    }

                    "list"(desc = "Get a list of all players allowed to interact with this display item.") {
                        playerAction {
                            val player = sender as Player
                            val entity = player.playerData.getRecentEntity() ?: return@playerAction
                            val locked = entity.toGeary().get<LockDisplayItem>() ?: return@playerAction

                            if (locked.lockState) {
                                player.success("The following people can interact with your ${entity.name}:")
                                locked.allowedAccess.forEach {
                                    player.info(it.toPlayer()?.name)
                                }
                            } else {
                                player.error("This ${entity.name} is not protected.")
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
