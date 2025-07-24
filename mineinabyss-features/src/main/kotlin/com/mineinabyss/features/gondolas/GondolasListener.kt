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
import com.mineinabyss.idofront.textcomponents.miniMsg

class GondolasListener : Listener {
    private val playerZoneEntry = mutableMapOf<UUID, Pair<String, Long>>()
    private val justWarped = mutableSetOf<UUID>()
    private val lastErrorTime = mutableMapOf<UUID, Long>()
    private val errorCooldown = 5000

    @EventHandler
    fun PlayerMoveEvent.onPlayerMove() {
        if (!hasExplicitlyChangedBlock()) return

        val gondolas = LoadedGondolas.loaded
        val now = System.currentTimeMillis()

        val nearbyGondolaData = gondolas.entries.asSequence()
            .map { (id, gondola) -> getClosestGondolaData(gondola, player.location, id) }
            .firstOrNull { it.type != GondolaType.NONE } ?: run {
            playerZoneEntry.remove(player.uniqueId)
            justWarped.remove(player.uniqueId)
            return
        }

        val unlockedGondolas = player.toGeary().get<UnlockedGondolas>() ?: return
        if (nearbyGondolaData.id !in unlockedGondolas.keys) return showError(player, nearbyGondolaData.gondola, now)

        if (player.uniqueId in justWarped) return
        handleWarpCooldown(player, nearbyGondolaData, now)
    }

    private fun showError(player: Player, gondola: Gondola, now: Long) {
        val lastTime = lastErrorTime[player.uniqueId] ?: 0L
        if (now - lastTime >= errorCooldown) {
            player.error(gondola.rawNoAccessMessage)
            lastErrorTime[player.uniqueId] = now
        }
    }

    private fun handleWarpCooldown(
        player: Player,
        data: GondolaData,
        now: Long
    ) {
        val entry = playerZoneEntry[player.uniqueId]

        when {
            entry?.first != data.id -> {
                playerZoneEntry[player.uniqueId] = data.id to now
            }

            now - entry.second >= data.gondola.warpCooldown -> {
                gondolaWarp(data.gondola, player, data.type)
                playerZoneEntry.remove(player.uniqueId)
                justWarped.add(player.uniqueId)
            }

            else -> {
                val remainingSeconds = (data.gondola.warpCooldown - (now - entry.second)) / 1000
                player.sendActionBar("Warping in $remainingSeconds seconds...")
            }
        }
    }
}