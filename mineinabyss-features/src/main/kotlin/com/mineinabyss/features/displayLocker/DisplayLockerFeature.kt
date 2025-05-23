package com.mineinabyss.features.displayLocker

import com.mineinabyss.components.displaylocker.LockDisplayItem
import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.playerDataOrNull
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.commands.arguments.offlinePlayerArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.plugin.unregisterListeners
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class DisplayLockerFeature : Feature() {

    private val listeners = listOf(DisplayLockerListener(), BookshelfLocker())

    override fun FeatureDSL.enable() {
        plugin.listeners(*listeners.toTypedArray())

        mainCommand {
            "lock"(desc = "Protection related commands") {
                "default_state"(desc = "Toggles the default lockstate of player") {
                    playerAction {
                        player.editPlayerData {
                            defaultDisplayLockState = !defaultDisplayLockState
                            player.success("Your default lockstate is now ${if (defaultDisplayLockState) "locked" else "unlocked"}")
                        }
                    }
                }
                "toggle"(desc = "Toggles if a display item should be protected or not") {
                    playerAction {
                        val player = sender as Player
                        val entity = player.playerDataOrNull?.getRecentEntity() ?: return@playerAction
                        val locked = entity.toGeary().get<LockDisplayItem>() ?: return@playerAction
                        locked.lockState = !locked.lockState
                        entity.setGravity(!locked.lockState)

                        if (locked.lockState) player.success("This ${entity.name} is now protected!")
                        else player.error("This ${entity.name} is no longer protected!")
                    }
                }
                "add"(desc = "Add a player to this display item.") {
                    val offlinePlayer by offlinePlayerArg()
                    playerAction {
                        val player = sender as Player
                        val entity = player.playerDataOrNull?.getRecentEntity() ?: return@playerAction
                        val locked = entity.toGeary().get<LockDisplayItem>() ?: return@playerAction
                        val uuid = offlinePlayer.uniqueId

                        if (uuid in locked.allowedAccess)
                            return@playerAction player.error("${offlinePlayer.name} can already interact with this ${entity.name}")

                        locked.allowedAccess.add(uuid)
                        entity.toGeary().encodeComponentsTo(entity.persistentDataContainer)
                        player.success("${offlinePlayer.name} can now interact with this ${entity.name}")
                    }
                }

                "remove"(desc = "Remove a player to this display item.") {
                    val offlinePlayer by offlinePlayerArg()
                    playerAction {
                        val player = sender as Player
                        val entity = player.playerDataOrNull?.getRecentEntity() ?: return@playerAction
                        val locked = entity.toGeary().get<LockDisplayItem>() ?: return@playerAction
                        val uuid = offlinePlayer.uniqueId

                        if (uuid in locked.allowedAccess) {
                            locked.allowedAccess.remove(uuid)
                            entity.toGeary().encodeComponentsTo(entity.persistentDataContainer)
                            player.success("${offlinePlayer.name} has been removed from this ${entity.name}")
                        } else player.error("${offlinePlayer.name} cannot interact with this ${entity.name}")
                    }
                }

                "clear"(desc = "Clear all other players from this display item") {
                    playerAction {
                        val player = sender as Player
                        val entity = player.playerDataOrNull?.getRecentEntity() ?: return@playerAction
                        val locked = entity.toGeary().get<LockDisplayItem>() ?: return@playerAction

                        locked.allowedAccess.clear()
                        locked.allowedAccess.add(locked.owner)
                        entity.toGeary().encodeComponentsTo(entity.persistentDataContainer)
                        player.success("All players were removed from this ${entity.name}")
                    }
                }

                "list"(desc = "Get a list of all players allowed to interact with this display item.") {
                    playerAction {
                        val player = sender as Player
                        val entity = player.playerDataOrNull?.getRecentEntity() ?: return@playerAction
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
                1 -> listOf("lock").filter { it.startsWith(args[0]) }

                2 -> when (args[0]) {
                    "lock" -> listOf("add", "remove", "clear", "toggle", "default_state")
                    else -> null
                }?.filter { it.startsWith(args[1]) }

                3 -> when (args[1]) {
                    "add" -> Bukkit.getOnlinePlayers().map { it.name }
                    "remove" -> Bukkit.getOnlinePlayers().map { it.name }
                    else -> null
                }?.filter { it.lowercase().startsWith(args[2].lowercase()) }

                else -> null
            }
        }
    }

    override fun FeatureDSL.disable() {
        plugin.unregisterListeners(*listeners.toTypedArray())
    }
}
