package com.mineinabyss.enchants.enchantments

import com.mineinabyss.components.mobs.Splitjaw
import com.mineinabyss.enchants.CustomEnchants
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class JawBreakerListener : Listener {

    @EventHandler
    fun EntityDamageByEntityEvent.onSplitjawHit() {
        val player = damager as? Player ?: return
        val item = player.inventory.itemInMainHand
        entity.toGearyOrNull()?.get<Splitjaw>() ?: return

        if (CustomEnchants.JAW_BREAKER in item.enchantments)
            damage += item.getEnchantmentLevel(CustomEnchants.JAW_BREAKER) * 2

    }
}
