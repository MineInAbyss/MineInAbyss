package com.derongan.minecraft.mineinabyss.systems

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.mineinabyss.components.Orthbound
import com.derongan.minecraft.mineinabyss.configuration.MIAConfig
import com.mineinabyss.geary.minecraft.components.Soulbound
import com.mineinabyss.geary.minecraft.store.encodeComponentsTo
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.looty.tracking.toGearyFromUUIDOrNull
import org.bukkit.ChatColor
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemFlag

object SoulbindListener : Listener {
    @EventHandler
    fun PlayerAscendEvent.soulbindItems() {
        if (toSection != MIAConfig.data.hubSection) return
        player.inventory.contents.filterNotNull().forEach { it ->
            val item = it.toGearyFromUUIDOrNull() ?: return
            item.get<Orthbound>() ?: return@forEach
            item.setPersisting(Soulbound(owner = player.uniqueId))
            it.editItemMeta { lore = listOf("${ChatColor.RED}Soulbound", "") }
            it.editMeta { it.addEnchant(Enchantment.ARROW_DAMAGE, 1, false) } // Sucky solution
            it.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            item.get<Soulbound>()?.owner.broadcastVal("owner: ")
            item.encodeComponentsTo(it)
        }
    }
}
