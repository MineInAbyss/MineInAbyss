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

        val nearbyGondolaData =
            gondolas.entries.firstNotNullOfOrNull { (id, gondola) ->
                val type = getClosestGondolaType(gondola, player.location)
                if (type != GondolaType.NONE) Triple(
                    id,
                    gondola,
                    type
                ) else null
            }

        if (nearbyGondolaData == null) {
            playerZoneEntry.remove(player.uniqueId)
            justWarped.remove(player.uniqueId)
            return
        }

        val (id, gondola, type) = nearbyGondolaData

        val unlockedGondolas = player.toGeary().get<UnlockedGondolas>() ?: return
        if (id !in unlockedGondolas.keys) return showError(player, gondola, now)

        if (player.uniqueId in justWarped) return
        handleWarpCooldown(player, id, gondola, type, now)
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
        id: String,
        gondola: Gondola,
        type: GondolaType,
        now: Long
    ) {
        val entry = playerZoneEntry[player.uniqueId]

        when {
            entry?.first != id -> {
                playerZoneEntry[player.uniqueId] = id to now
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