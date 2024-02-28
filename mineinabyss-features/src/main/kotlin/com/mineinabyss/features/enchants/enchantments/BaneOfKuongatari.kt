package com.mineinabyss.features.enchants.enchantments

import com.mineinabyss.components.mobs.Insect
import com.mineinabyss.features.enchants.BaneOfKuongatari
import com.mineinabyss.features.enchants.CustomEnchants
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class BaneOfKuongatariListener : Listener {

    @EventHandler
    fun EntityDamageByEntityEvent.onInsectHit() {
        val player = damager as? Player ?: return
        val item = player.inventory.toGeary()?.itemInMainHand ?: return
        entity.toGearyOrNull()?.get<Insect>() ?: return

        // Ideally this would use getDamageIncrease function
        val enchant = CustomEnchants.get<BaneOfKuongatari>(item) ?: return
        damage += enchant.level * 2
    }
}
