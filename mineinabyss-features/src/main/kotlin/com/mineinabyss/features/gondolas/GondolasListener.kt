package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.messaging.error
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerMoveEvent
import java.util.UUID

class GondolasListener : Listener {
    private val playerZoneEntry = mutableMapOf<UUID, Pair<String, Long>>()
    private val justWarped = mutableSetOf<UUID>()
    private val lastErrorTime = mutableMapOf<UUID, Long>()
    private val errorCooldown = 5000

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!event.hasExplicitlyChangedBlock()) return

        val player = event.player
        val gondolas = LoadedGondolas.loaded
        val now = System.currentTimeMillis()

        // iterate through all gondolas
        for (gondola in gondolas.values) {
            val unlockedGondolas =
                player.toGeary().get<UnlockedGondolas>() ?: continue
            // finds if  we are near a gondola upper/lower section
            // type = GondolaType.NONE if not near a gondola
            // type = GondolaType.UPPER if near upper section
            // type = GondolaType.LOWER if near lower section
            val type = getClosestGondolaType(gondola, player.location)
            if (type != GondolaType.NONE) {
                // ensure the player has access to the gondola
                if (gondola.name !in unlockedGondolas.keys) {
                    val lastTime = lastErrorTime[player.uniqueId] ?: 0L
                    if (now - lastTime >= errorCooldown) {
                        player.error(gondola.noAccessMessage)
                        lastErrorTime[player.uniqueId] = now
                    }
                    return
                }
                // warp cooldown
                if (justWarped.contains(player.uniqueId)) return

                // apply and check warp cooldown
                val entry = playerZoneEntry[player.uniqueId]
                if (entry?.first == gondola.name) {
                    if (now - entry.second >= gondola.warpCooldown) {
                        gondolaWarp(gondola, player, type)
                        playerZoneEntry.remove(player.uniqueId)
                        justWarped.add(player.uniqueId)
                        return
                    } else {
                        val message =
                            "Warping in ${((gondola.warpCooldown - (now - entry.second)) / 1000)} seconds..."
                        player.sendActionBar(message)
                        return
                    }
                } else {
                    playerZoneEntry[player.uniqueId] = gondola.name to now
                }
                return
            }
        }
        playerZoneEntry.remove(player.uniqueId)
        justWarped.remove(player.uniqueId)
    }
}