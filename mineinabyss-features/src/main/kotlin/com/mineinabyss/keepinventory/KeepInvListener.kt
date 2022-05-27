package com.mineinabyss.keepinventory

import com.mineinabyss.components.playerData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent

class KeepInvListener(private val feature: KeepInvFeature) : Listener {
    @EventHandler
    fun PlayerDeathEvent.optionalKeepInventory() {
        if ((feature.KeepInvInVoid && player.lastDamageCause?.cause == EntityDamageEvent.DamageCause.VOID) ||
            player.playerData.keepInvStatus) {
            keepInventory = true
            drops.clear()
            keepLevel = true
            droppedExp = 0
        } else keepInventory = false
    }
}
