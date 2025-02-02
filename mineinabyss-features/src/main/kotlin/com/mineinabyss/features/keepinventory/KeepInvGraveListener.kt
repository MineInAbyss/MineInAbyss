package com.mineinabyss.features.keepinventory

import com.mineinabyss.components.playerDataOrNull
import com.mineinabyss.eternalfortune.api.events.PlayerCreateGraveEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class KeepInvGraveListener(private val config: KeepInvFeature.Config): Listener {
    @EventHandler
    fun PlayerCreateGraveEvent.onCreateGrave() {
        if ((config.keepInvInVoid && player.lastDamageCause?.cause == EntityDamageEvent.DamageCause.VOID) ||
            player.playerDataOrNull?.keepInvStatus != false
        ) {
            isCancelled = true
        }
    }
}
