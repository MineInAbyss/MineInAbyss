package com.mineinabyss.features.whistle

import com.mineinabyss.features.whistle.repository.WhistleRepository
import com.mineinabyss.idofront.messaging.info
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class WhistleListener(
    private val repository: WhistleRepository,
) : Listener {

    @EventHandler
    suspend fun PlayerJoinEvent.onJoin() = repository.loadPlayer(player)

    @EventHandler
    fun PlayerQuitEvent.onQuit() = repository.unloadPlayer(player)

    @EventHandler
    fun EntityDamageEvent.onFallDamage() {
        if (cause != EntityDamageEvent.DamageCause.FALL) {
            val player = entity as? Player ?: return
            player.info(("you did not fall!!"))
            return
        }
        val player = entity as? Player ?: return
        // todo: cleanup, move multipliers into WhistleData buff config
        when (repository.whistleId(player)) {
            "blueWhistle" -> damage *= 1 - 0.12
            "whiteWhistle" -> damage *= 1 - 0.5
        }
    }
}
