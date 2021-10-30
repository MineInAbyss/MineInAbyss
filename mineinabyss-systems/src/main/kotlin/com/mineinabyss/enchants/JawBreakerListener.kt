package com.derongan.minecraft.mineinabyss.systems

import com.derongan.minecraft.mineinabyss.components.Splitjaw
import com.mineinabyss.enchants.CustomEnchants
import com.mineinabyss.geary.minecraft.access.toGearyOrNull
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent

class JawBreakerListener : Listener {

    @EventHandler
    fun EntityDamageByEntityEvent.onSplitjawHit() {
        val player = damager as? Player ?: return
        val splitjaw = entity.toGearyOrNull()?.get<Splitjaw>() ?: return
        val item = player.inventory.itemInMainHand
        val jawBreaker = CustomEnchants.JAW_BREAKER

        if (item.containsEnchantment(jawBreaker)) {
            damage += item.getEnchantmentLevel(jawBreaker)
        }

    }

    @EventHandler
    fun EntityDeathEvent.onSplitjawDeath() {

    }
}