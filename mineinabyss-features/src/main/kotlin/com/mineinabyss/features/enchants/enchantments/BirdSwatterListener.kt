package com.mineinabyss.features.enchants.enchantments

import com.mineinabyss.components.mobs.Bird
import com.mineinabyss.features.enchants.BirdSwatter
import com.mineinabyss.features.enchants.CustomEnchants
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class BirdSwatterListener : Listener {

    @EventHandler
    fun EntityDamageByEntityEvent.onBirdHit() {
        val player = damager as? Player ?: return
        val item = player.inventory.toGeary()?.itemInMainHand ?: return
        entity.toGearyOrNull()?.get<Bird>() ?: return

        // Ideally this would use getDamageIncrease function
        val enchant = CustomEnchants.get<BirdSwatter>(item) ?: return
        damage += enchant.level * 2
    }
}
