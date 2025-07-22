package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.messaging.error
import org.bukkit.entity.Player
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

        val nearbyGondola = gondolas.values.firstNotNullOfOrNull { gondola ->
            val type = getClosestGondolaType(gondola, player.location)
            if (type != GondolaType.NONE) gondola to type else null
        }

        if (nearbyGondola == null) {
            playerZoneEntry.remove(player.uniqueId)
            justWarped.remove(player.uniqueId)
            return
        }

        val (gondola, type) = nearbyGondola

        val unlockedGondolas =
            player.toGeary().get<UnlockedGondolas>() ?: return
        if (gondola.name !in unlockedGondolas.keys) {
            showError(player, gondola, now)
            return
        }

        if (justWarped.contains(player.uniqueId)) return
        handleWarpCooldown(player, gondola, type, now)
    }

    private fun showError(player: Player, gondola: Gondola, now: Long) {
        val lastTime = lastErrorTime[player.uniqueId] ?: 0L
        if (now - lastTime >= errorCooldown) {
            player.error(gondola.noAccessMessage)
            lastErrorTime[player.uniqueId] = now
        }
    }

    private fun handleWarpCooldown(
        player: Player,
        gondola: Gondola,
        type: GondolaType,
        now: Long
    ) {
        val entry = playerZoneEntry[player.uniqueId]

        when {
            entry?.first != gondola.name -> {
                playerZoneEntry[player.uniqueId] = gondola.name to now
            }

            now - entry.second >= gondola.warpCooldown -> {
                gondolaWarp(gondola, player, type)
                playerZoneEntry.remove(player.uniqueId)
                justWarped.add(player.uniqueId)
            }

            else -> {
                val remainingSeconds =
                    (gondola.warpCooldown - (now - entry.second)) / 1000
                player.sendActionBar("Warping in $remainingSeconds seconds...")
            }
        }
    }
}