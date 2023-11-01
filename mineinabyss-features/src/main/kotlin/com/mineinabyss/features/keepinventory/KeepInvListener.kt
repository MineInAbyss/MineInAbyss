package com.mineinabyss.features.keepinventory

import com.mineinabyss.components.playerData
import org.bukkit.GameRule
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.world.WorldLoadEvent

class KeepInvListener(private val config: KeepInvFeature.Config) : Listener {
    // Force keepinv to be false
    @EventHandler
    fun WorldLoadEvent.onWorldLoad() {
        world.setGameRule(GameRule.KEEP_INVENTORY, false)
    }

    @EventHandler
    fun PlayerDeathEvent.optionalKeepInventory() {
        if ((config.keepInvInVoid && player.lastDamageCause?.cause == EntityDamageEvent.DamageCause.VOID) ||
            player.playerData.keepInvStatus) {
            keepInventory = true
            keepLevel = true
            droppedExp = 0
            itemsToKeep.addAll(drops)
            drops.clear()
        }
    }
}

