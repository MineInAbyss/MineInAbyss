package com.mineinabyss.features.enchants.enchantments

import com.mineinabyss.components.mobs.Splitjaw
import com.mineinabyss.features.enchants.CustomEnchants
import com.mineinabyss.features.enchants.JawBreaker
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class JawBreakerListener : Listener {

    @EventHandler
    fun EntityDamageByEntityEvent.onSplitjawHit() {
        val player = damager as? Player ?: return
        val item = player.inventory.toGeary()?.itemInMainHand ?: return
        entity.toGearyOrNull()?.get<Splitjaw>() ?: return

        val enchant = CustomEnchants.get<JawBreaker>(item) ?: return
        damage += enchant.level * 2
    }
}
