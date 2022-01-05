package com.mineinabyss.enchants

import com.mineinabyss.idofront.messaging.error
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.meta.EnchantmentStorageMeta

class EnchantmentListener : Listener {

    @EventHandler
    fun InventoryClickEvent.applyCustomEnchantmentBook() {
        val player = whoClicked as Player
        val anvil = inventory as AnvilInventory
        val book = (anvil.secondItem!!.itemMeta as EnchantmentStorageMeta).storedEnchants
        val target = getItemTarget(anvil.firstItem)

        if (inventory.type != InventoryType.ANVIL) return
        if (anvil.secondItem?.type != Material.ENCHANTED_BOOK) return

        book.forEach {
            val enchant = it.component1()
            val enchantLevel = it.component2()
            var newLevel = enchantLevel
            if (it.component1().itemTarget != target && slot == 2) {
                player.error("This book cannot be added to this item")
                anvil.maximumRepairCost = 0
                player.closeInventory()
                anvil.result = null

                isCancelled = true
                return@forEach
            }

            if (anvil.firstItem?.containsEnchantment(enchant) == true) {
                val itemLevel = anvil.firstItem!!.getEnchantmentLevel(enchant)

                newLevel = when {
                    itemLevel == enchantLevel -> itemLevel + 1
                    itemLevel + 1 == enchantLevel -> itemLevel + 1
                    itemLevel > enchantLevel -> itemLevel
                    itemLevel * 2 <= enchantLevel -> enchantLevel
                    else -> itemLevel
                }
            }
            anvil.firstItem!!.removeCustomEnchant(enchant)
            anvil.firstItem!!.addCustomEnchant(enchant as EnchantmentWrapper, newLevel)
            val resultItem = anvil.firstItem
            anvil.result = resultItem
        }

    }
}