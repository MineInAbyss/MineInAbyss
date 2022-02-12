package com.mineinabyss.keepinventory

import com.mineinabyss.components.playerData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent

class KeepInvListener : Listener {
    @EventHandler
    fun PlayerDeathEvent.optionalKeepInventory() {
        if (player.playerData.keepInvStatus) {
            keepInventory = true
            drops.clear()
        }
        else keepInventory = false

        //TODO maybe limit this to only the survival server with a config option
        if (player.lastDamageCause?.cause == EntityDamageEvent.DamageCause.VOID) keepInventory = true
    }
}