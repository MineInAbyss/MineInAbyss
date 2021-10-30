package com.derongan.minecraft.mineinabyss.systems

import com.derongan.minecraft.mineinabyss.components.Insect
import com.mineinabyss.enchants.CustomEnchants
import com.mineinabyss.geary.minecraft.access.toGearyOrNull
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class BaneOfKuongatariListener : Listener {

    @EventHandler
    fun EntityDamageByEntityEvent.onInsectHit() {
        val player = damager as? Player ?: return
        val insect = entity.toGearyOrNull()?.get<Insect>() ?: return
        val item = player.inventory.itemInMainHand
        val baneOfKuon = CustomEnchants.BANE_OF_KUONGATARI

        // Ideally this would use getDamageIncrease function
        if (item.containsEnchantment(baneOfKuon)) damage += item.getEnchantmentLevel(baneOfKuon) * 2
    }

}