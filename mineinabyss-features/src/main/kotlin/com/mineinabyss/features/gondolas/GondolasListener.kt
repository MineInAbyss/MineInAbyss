package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.features.gondolas.pass.TicketConfig
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.messaging.error
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerMoveEvent
import java.util.UUID
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.sound.Sound
import org.bukkit.plugin.java.JavaPlugin
import kotlin.collections.remove
import kotlin.compareTo
import kotlin.div
import kotlin.text.get
import net.kyori.adventure.key.Key
import org.bukkit.Location

class GondolasListener : Listener {
    private val playerZoneEntry = mutableMapOf<UUID, Pair<String, Long>>()
    private val justWarped = mutableSetOf<UUID>()
    private val lastErrorTime = mutableMapOf<UUID, Long>()
    private val errorCooldown = 5000

    private fun handleGondola(player: Player) {
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

    fun startCooldownDisplayTask(plugin: JavaPlugin) {
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            val now = System.currentTimeMillis()
            for ((uuid, entry) in playerZoneEntry) {
                val player = plugin.server.getPlayer(uuid) ?: continue
                val gondolaData = LoadedGondolas.loaded[entry.first] ?: continue
                val remaining = gondolaData.warpCooldown - (now - entry.second)
                if (remaining == gondolaData.warpCooldown) {
                    val soundKey = Key.key("minecraft:ambient.cave.cave_18")
                    player.playSound(Sound.sound(soundKey,Sound.Source.AMBIENT,1f,1f))
                }
                if (remaining > 0) {
                    val seconds = remaining / 1000
                    player.sendActionBar("Warping in $seconds seconds...")
                } else {
                    handleGondola(player)
                }
            }
        }, 1L, 1L)
    }
    
    private fun handleWarpCooldown(
        player: Player,
        data: GondolaData,
        now: Long,
        gondolaId: String? = null,
    ) {
        val entry = playerZoneEntry[player.uniqueId]

        when {
            entry?.first != data.id -> {
                playerZoneEntry[player.uniqueId] = data.id to now
            }

            now - entry.second >= data.gondola.warpCooldown -> {
                gondolaWarp(data.gondola, player, data.type, gondolaId)
                playerZoneEntry.remove(player.uniqueId)
                justWarped.add(player.uniqueId)
            }
        }
    }
}