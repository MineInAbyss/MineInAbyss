package com.mineinabyss.displayLocker

import com.mineinabyss.components.armorstandlock.LockArmorStand
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.store.encodeComponentsTo
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import net.minecraft.util.DirectoryLock.LockException
import org.bukkit.Bukkit
import org.bukkit.entity.Player


@kotlinx.serialization.Serializable
@SerialName("lock_display_item")
class DisplayLockerFeature: AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(DisplayLockerListener())

        commands {
            mineinabyss {
                "lock"(desc="display locker commands") {
                    val player = sender as Player
                    val entity = player.playerData.recentRightclickedEntity
                    val locked = entity?.toGeary()?.get<LockArmorStand>()

                    "toggle"(desc="toggles locking on display item") playerAction@{
                        locked?.lockState = !locked?.lockState!!
                        entity.setGravity(!locked.lockState)
                        entity.toGeary().encodeComponentsTo(entity)
                        if (locked.lockState) player.success("this ${entity.name} is now protected!")
                        else player.error("this ${entity.name} is no longer protected!")
                    }
                    "add"(desc="add a player to this display item") {
                        val playerName by stringArg()
                        val uuid = Bukkit.getOfflinePlayer(playerName).uniqueId

                        if (!locked?.isAllowed(uuid)!!) {
                            locked.allowedAccess.add(uuid)
                            entity.toGeary().encodeComponentsTo(entity)
                            player.success("$playerName is now able to interact with this ${entity.name}.")
                        } else player.error("$playerName is already able to interact with this ${entity.name}")
                    }
                    "remove"(desc="remove a player from this display item") {
                        val playerName by stringArg {parseErrorMessage = { "There is no player with the name: $passed." }}
                        val uuid = Bukkit.getOfflinePlayer(playerName).uniqueId

                        if (!locked?.isAllowed(uuid)!!) player.error("$playerName is unable to interact with this ${entity.name}.")
                        else {
                            locked.allowedAccess.remove(uuid)
                            entity.toGeary().encodeComponentsTo(entity)
                            player.success("$playerName was successfully removed from this ${entity.name}.")
                        }
                    }
                    "clear"(desc="clear all other players from this display item") {
                        locked?.allowedAccess?.clear()
                        locked?.allowedAccess?.add(locked.owner)
                        entity?.toGeary()?.encodeComponentsTo(entity)
                        player.success("all other players were successfully removed from this ${entity?.name}.")
                    }
                    "list"(desc="list all players that are currently able to interact with this display item") {
                        if (!locked?.lockState!!) player.error("this ${entity.name} is currently unprotected, anyone can interact with it.")
                        else {
                            player.success("The following people are able to interact with your ${entity.name}:")
                            locked.allowedAccess.forEach {
                                player.info(it.toPlayer()?.name)
                            }
                        }
                    }
                }
            }
        }
    }
}