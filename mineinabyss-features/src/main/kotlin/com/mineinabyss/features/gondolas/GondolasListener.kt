package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.features.abyss
import com.mineinabyss.features.gondolas.GondolasHelpers.gondolaWarp
import com.mineinabyss.features.gondolas.pass.TicketConfigHolder
import com.mineinabyss.geary.papermc.launchTickRepeating
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.messaging.error
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import java.util.*

private data class ZoneEntry(val id: String, val timestamp: Long)

class GondolasListener : Listener {
    private val playerZoneEntry = mutableMapOf<UUID, ZoneEntry>()
    private val justWarped = mutableSetOf<UUID>()
    private val lastErrorTime = mutableMapOf<UUID, Long>()
    private val errorCooldown = 5000

    private fun handleGondola(player: Player) {
        val gondolas = LoadedGondolas.loaded
        val now = System.currentTimeMillis()

        val nearbyGondolaData = gondolas.entries.asSequence()
            .map { (id, gondola) -> GondolasHelpers.closestGondolaData(gondola, player.location, id) }
            .firstOrNull { it.type != GondolaType.NONE } ?: run {
            playerZoneEntry.remove(player.uniqueId)
            justWarped.remove(player.uniqueId)
            return
        }

        val isUnlockedByDefault = TicketConfigHolder.config?.tickets?.values
            ?.any { nearbyGondolaData.id in it.gondolasInRoute && it.unlockedByDefault } == true
        if (!isUnlockedByDefault) {
            val unlockedGondolas = player.toGeary().get<UnlockedGondolas>() ?: return showError(player, nearbyGondolaData.gondola, now)
            if (nearbyGondolaData.id !in unlockedGondolas.keys) return showError(player, nearbyGondolaData.gondola, now)
        }

        if (player.uniqueId in justWarped) return
        handleWarpCooldown(player, nearbyGondolaData, now, nearbyGondolaData.id)
    }

    @EventHandler
    fun PlayerMoveEvent.onPlayerMove() {
        if (!hasExplicitlyChangedBlock()) return
        handleGondola(player)
    }

    private fun showError(player: Player, gondola: Gondola, now: Long) {
        val lastTime = lastErrorTime[player.uniqueId] ?: 0L
        if (now - lastTime >= errorCooldown) {
            player.error(gondola.noAccessMessage)
            lastErrorTime[player.uniqueId] = now
        }
    }

    private val warpSound = Sound.sound(Key.key("minecraft:ambient.cave.cave_18"), Sound.Source.AMBIENT, 1f, 1f)
    fun startCooldownDisplayTask() {
        abyss.launchTickRepeating(1L) {
            val now = System.currentTimeMillis()
            for ((uuid, entry) in playerZoneEntry) {
                val player = uuid.toPlayer() ?: continue
                val gondolaData = LoadedGondolas.loaded[entry.id] ?: continue
                val remaining = gondolaData.warpCooldown.inWholeMilliseconds - (now - entry.timestamp)

                if (remaining == gondolaData.warpCooldown.inWholeMilliseconds) player.playSound(warpSound)
                if (remaining > 0) player.sendActionBar(Component.text("Warping in ${remaining / 1000} seconds..."))
                else handleGondola(player)
            }
        }
    }

    private fun handleWarpCooldown(player: Player, data: GondolaData, now: Long, gondolaId: String? = null) {
        val entry = playerZoneEntry[player.uniqueId]

        when {
            entry?.id != data.id -> playerZoneEntry[player.uniqueId] = ZoneEntry(data.id, now)
            now - entry.timestamp >= data.gondola.warpCooldown.inWholeMilliseconds -> {
                gondolaWarp(data.gondola, player, data.type, gondolaId)
                playerZoneEntry.remove(player.uniqueId)
                justWarped.add(player.uniqueId)
            }
        }
    }
}