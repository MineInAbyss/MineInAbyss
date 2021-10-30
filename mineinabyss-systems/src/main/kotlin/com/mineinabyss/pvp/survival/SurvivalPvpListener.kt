package com.mineinabyss.pvp.survival

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.components.playerData
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.mineinabyss.core.layer
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class SurvivalPvpListener : Listener {
    @EventHandler
    fun PlayerDescendEvent.onEnterPvPLayer() {
        if (toSection.layer?.hasPvPDefault == true && !player.playerData.pvpStatus) {
            player.error("PVP is always enabled below this point.")
        }
    }

    @EventHandler
    fun PlayerAscendEvent.onEnterOrth() {
        val data = player.playerData
        data.showPvPMessage = data.pvpUndecided
    }

    @EventHandler
    fun EntityDamageByEntityEvent.playerCombatSystem() {
        val player = entity as? Player ?: return

        val attacker: Player = when (damager) {
            is Projectile -> {
                (damager as Projectile).shooter as? Player ?: return
            }
            is Player -> {
                (damager as Player)
            }
            else -> {
                return
            }
        }

        if ((player.location.layer?.hasPvPDefault == true)
            || (player.playerData.pvpStatus && attacker.playerData.pvpStatus)
            || player == attacker
        ) return

        isCancelled = true
    }
}