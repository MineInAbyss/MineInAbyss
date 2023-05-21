package com.mineinabyss.enchants.enchantments

import com.mineinabyss.components.mobs.Bird
import com.mineinabyss.enchants.CustomEnchants
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class BirdSwatterListener : Listener {

    @EventHandler
    fun EntityDamageByEntityEvent.onBirdHit() {
        val player = damager as? Player ?: return
        val item = player.inventory.itemInMainHand
        entity.toGearyOrNull()?.get<Bird>() ?: return

        // Ideally this would use getDamageIncrease function
        if (CustomEnchants.BIRD_SWATTER in item.enchantments)
            damage += item.getEnchantmentLevel(CustomEnchants.BIRD_SWATTER) * 2
    }
}
