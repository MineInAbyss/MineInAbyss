package com.mineinabyss.features.enchants.enchantments

import com.mineinabyss.components.mobs.Insect
import com.mineinabyss.features.enchants.CustomEnchants
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class BaneOfKuongatariListener : Listener {

    @EventHandler
    fun EntityDamageByEntityEvent.onInsectHit() {
        val player = damager as? Player ?: return
        val item = player.inventory.itemInMainHand
        entity.toGearyOrNull()?.get<Insect>() ?: return

        // Ideally this would use getDamageIncrease function
        if (CustomEnchants.BANE_OF_KUONGATARI in item.enchantments)
            damage += item.getEnchantmentLevel(CustomEnchants.BANE_OF_KUONGATARI) * 2
    }
}
