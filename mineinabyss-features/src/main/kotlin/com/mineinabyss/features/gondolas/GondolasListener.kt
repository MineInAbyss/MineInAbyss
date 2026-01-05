package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.features.abyss
import com.mineinabyss.features.gondolas.GondolasHelpers.gondolaWarp
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

class GondolasListener : Listener {
    private val playerZoneEntry = mutableMapOf<UUID, Pair<String, Long>>()
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

        val unlockedGondolas = player.toGeary().get<UnlockedGondolas>() ?: return
        if (nearbyGondolaData.id !in unlockedGondolas.keys) return showError(player, nearbyGondolaData.gondola, now)

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
            player.error(gondola.rawNoAccessMessage)
            lastErrorTime[player.uniqueId] = now
        }
    }

    fun startCooldownDisplayTask() {
        abyss.plugin.launchTickRepeating(1L) {
            val now = System.currentTimeMillis()
            for ((uuid, entry) in playerZoneEntry) {
                val player = uuid.toPlayer() ?: continue
                val gondolaData = LoadedGondolas.loaded[entry.first] ?: continue
                val remaining = gondolaData.warpCooldown - (now - entry.second)
                if (remaining == gondolaData.warpCooldown) {
                    val soundKey = Key.key("minecraft:ambient.cave.cave_18")
                    player.playSound(Sound.sound(soundKey,Sound.Source.AMBIENT,1f,1f))
                }
                if (remaining > 0) player.sendActionBar(Component.text("Warping in ${remaining / 1000} seconds..."))
                else handleGondola(player)
            }
        }
    }
    
    private fun handleWarpCooldown(player: Player, data: GondolaData, now: Long, gondolaId: String? = null) {
        val entry = playerZoneEntry[player.uniqueId]

        when {
            entry?.first != data.id -> playerZoneEntry[player.uniqueId] = data.id to now
            now - entry.second >= data.gondola.warpCooldown -> {
                gondolaWarp(data.gondola, player, data.type, gondolaId)
                playerZoneEntry.remove(player.uniqueId)
                justWarped.add(player.uniqueId)
            }
        }
    }
}