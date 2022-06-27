package com.mineinabyss.pvp

import com.mineinabyss.components.playerData
import com.mineinabyss.mineinabyss.core.layer
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

        if ((player.location.layer?.hasPvpDefault == true)
            || (player.playerData.pvpStatus && attacker.playerData.pvpStatus)
            || player == attacker
        ) return

        isCancelled = true
    }
}
