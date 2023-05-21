package com.mineinabyss.enchants.enchantments

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.enchants.CustomEnchants
import com.mineinabyss.mineinabyss.core.abyss
import kotlinx.coroutines.delay
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import kotlin.time.Duration.Companion.seconds

class FrostAspectListener : Listener {
    @EventHandler(ignoreCancelled = true)
    fun EntityDamageByEntityEvent.onFrostAspectHit() {
        val item = (damager as? Player ?: return).inventory.itemInMainHand
        val totalFreezeTicks = 5 * item.getEnchantmentLevel(CustomEnchants.FROST_ASPECT)
        var freezeTicks = 0

        if (CustomEnchants.FROST_ASPECT in item.enchantments) {
            entity.freezeTicks = entity.maxFreezeTicks
            entity.lockFreezeTicks(true)

            abyss.plugin.launch {
                do {
                    delay(1.seconds)
                    (entity as LivingEntity).damage(1.0)
                    freezeTicks += 1
                } while (freezeTicks <= totalFreezeTicks)
                entity.lockFreezeTicks(false)
            }
        }
    }
}
