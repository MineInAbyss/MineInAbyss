package com.derongan.minecraft.mineinabyss.systems

import com.derongan.minecraft.mineinabyss.components.Bird
import com.mineinabyss.enchants.CustomEnchants
import com.mineinabyss.geary.minecraft.access.toGearyOrNull
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class BirdSwatterListener : Listener {

    @EventHandler
    fun EntityDamageByEntityEvent.onBirdHit() {
        val player = damager as? Player ?: return
        val bird = entity.toGearyOrNull()?.get<Bird>() ?: return
        val item = player.inventory.itemInMainHand
        val birdSwatter = CustomEnchants.BIRD_SWATTER

        // Ideally this would use getDamageIncrease function
        if (item.containsEnchantment(birdSwatter)) damage += item.getEnchantmentLevel(birdSwatter) * 2
    }

}