package com.mineinabyss.features.pvp

import com.mineinabyss.components.playerDataOrNull
import com.mineinabyss.features.helpers.layer
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class PvpDamageListener : Listener {
    @EventHandler
    fun EntityDamageByEntityEvent.playerCombatSystem() {
        val player = entity as? Player ?: return

        val attacker: Player = when (damager) {
            is Projectile -> (damager as Projectile).shooter as? Player ?: return
            is Player -> damager as Player
            else -> return
        }

        val playerPvpStatus = player.playerDataOrNull?.pvpStatus ?: false
        val attackerPvpStatus = attacker.playerDataOrNull?.pvpStatus ?: false
        if ((player.location.layer?.hasPvpDefault == true)
            || (playerPvpStatus && attackerPvpStatus)
            || player == attacker
        ) return

        isCancelled = true
    }
}
