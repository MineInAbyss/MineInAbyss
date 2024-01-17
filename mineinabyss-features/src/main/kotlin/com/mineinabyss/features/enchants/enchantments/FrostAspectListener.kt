package com.mineinabyss.features.enchants.enchantments

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.features.abyss
import com.mineinabyss.features.enchants.CustomEnchants
import com.mineinabyss.features.enchants.FrostAspect
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
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
        val player = (damager as? Player ?: return)
        val item = player.inventory.toGeary()?.itemInMainHand ?: return


        val enchant = CustomEnchants.get<FrostAspect>(item) ?: return
        val totalFreezeTicks = 5 * enchant.level
        var freezeTicks = 0
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
