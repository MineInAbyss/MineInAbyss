package com.mineinabyss.features.displayLocker

import com.mineinabyss.components.displaylocker.LockDisplayItem
import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.playerDataOrNull
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.playerExecutes
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import org.bukkit.entity.Player

val DisplayLockerFeature = feature("display-locker") {
    onEnable {
        listeners(DisplayLockerListener(), BookshelfLocker())
    }

    mainCommand {
        "lock" {
            description = "Protection related commands"
            "default_state" {
                description = "Toggles the default lockstate of player"
                playerExecutes {
                    player.editPlayerData {
                        defaultDisplayLockState = !defaultDisplayLockState
                        player.success("Your default lockstate is now ${if (defaultDisplayLockState) "locked" else "unlocked"}")
                    }
                }
            }
            "toggle" {
                description = "Toggles if a display item should be protected or not"
                playerExecutes {
                    val player = sender as Player
                    val entity = player.playerDataOrNull?.getRecentEntity() ?: return@playerExecutes
                    val locked = entity.toGeary().get<LockDisplayItem>() ?: return@playerExecutes
                    locked.lockState = !locked.lockState
                    entity.setGravity(!locked.lockState)

                    if (locked.lockState) player.success("This ${entity.name} is now protected!")
                    else player.error("This ${entity.name} is no longer protected!")
                }
            }
            "add" {
                description = "Add a player to this display item."
                playerExecutes(Args.offlinePlayer()) { offlinePlayer ->
                    val player = sender as Player
                    val entity = player.playerDataOrNull?.getRecentEntity() ?: return@playerExecutes
                    val locked = entity.toGeary().get<LockDisplayItem>() ?: return@playerExecutes
                    val uuid = offlinePlayer.uniqueId

                    if (uuid in locked.allowedAccess)
                        return@playerExecutes player.error("${offlinePlayer.name} can already interact with this ${entity.name}")

                    locked.allowedAccess.add(uuid)
                    entity.toGeary().encodeComponentsTo(entity.persistentDataContainer)
                    player.success("${offlinePlayer.name} can now interact with this ${entity.name}")
                }
            }

            "remove" {
                description = "Remove a player to this display item."
                playerExecutes(Args.offlinePlayer()) { offlinePlayer ->
                    val player = sender as Player
                    val entity = player.playerDataOrNull?.getRecentEntity() ?: return@playerExecutes
                    val locked = entity.toGeary().get<LockDisplayItem>() ?: return@playerExecutes
                    val uuid = offlinePlayer.uniqueId

                    if (uuid in locked.allowedAccess) {
                        locked.allowedAccess.remove(uuid)
                        entity.toGeary().encodeComponentsTo(entity.persistentDataContainer)
                        player.success("${offlinePlayer.name} has been removed from this ${entity.name}")
                    } else player.error("${offlinePlayer.name} cannot interact with this ${entity.name}")
                }
            }

            "clear" {
                description = "Clear all other players from this display item"
                playerExecutes {
                    val player = sender as Player
                    val entity = player.playerDataOrNull?.getRecentEntity() ?: return@playerExecutes
                    val locked = entity.toGeary().get<LockDisplayItem>() ?: return@playerExecutes

                    locked.allowedAccess.clear()
                    locked.allowedAccess.add(locked.owner)
                    entity.toGeary().encodeComponentsTo(entity.persistentDataContainer)
                    player.success("All players were removed from this ${entity.name}")
                }
            }

            "list" {
                description = "Get a list of all players allowed to interact with this display item."
                playerExecutes {
                    val player = sender as Player
                    val entity = player.playerDataOrNull?.getRecentEntity() ?: return@playerExecutes
                    val locked = entity.toGeary().get<LockDisplayItem>() ?: return@playerExecutes

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
}
