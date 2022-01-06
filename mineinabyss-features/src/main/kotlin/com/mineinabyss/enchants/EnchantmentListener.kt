package com.mineinabyss.enchants

import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.messaging.error
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.GrindstoneInventory
import org.bukkit.inventory.meta.EnchantmentStorageMeta

class EnchantmentListener : Listener {

    @EventHandler
    fun InventoryClickEvent.applyCustomEnchantmentBook() {
        if (inventory.type != InventoryType.ANVIL) return
        val anvil = inventory as AnvilInventory
        if (anvil.firstItem == null || anvil.firstItem!!.type == Material.AIR) return

        val player = whoClicked as Player
        val target = getItemTarget(anvil.firstItem)
        val enchanted =
            if (anvil.secondItem?.type == Material.ENCHANTED_BOOK) (anvil.secondItem?.itemMeta as EnchantmentStorageMeta).storedEnchants
            else if (anvil.secondItem?.type != Material.ENCHANTED_BOOK && anvil.secondItem != null) {
                anvil.secondItem?.enchantments
            }
            else return

        if (anvil.firstItem?.type != anvil.secondItem?.type && anvil.secondItem?.type != Material.ENCHANTED_BOOK) return

        enchanted?.forEach {
            val enchant = it.component1()
            val enchantLevel = it.component2()
            var newLevel = enchantLevel

            if (it.component1().itemTarget != target && slot == 2 && anvil.firstItem?.type != Material.ENCHANTED_BOOK) {
                player.error("This book cannot be added to this item")
                anvil.maximumRepairCost = 0

                isCancelled = true
                return@forEach
            }


            if ((anvil.firstItem?.type != Material.ENCHANTED_BOOK && anvil.firstItem?.containsEnchantment(enchant) == true) ||
                (anvil.firstItem?.type == Material.ENCHANTED_BOOK && (anvil.firstItem?.itemMeta as EnchantmentStorageMeta).hasStoredEnchant(enchant))) {
                (anvil.firstItem?.type == Material.ENCHANTED_BOOK).broadcastVal("is book: ")
                val itemLevel = anvil.firstItem!!.getEnchantmentLevel(enchant)

                newLevel = when {
                    itemLevel == enchantLevel -> itemLevel + 1
                    itemLevel + 1 == enchantLevel -> itemLevel + 1
                    itemLevel > enchantLevel -> itemLevel
                    itemLevel * 2 <= enchantLevel -> enchantLevel
                    itemLevel + enchantLevel >= enchant.maxLevel -> enchant.maxLevel
                    else -> itemLevel
                }
                anvil.firstItem?.removeCustomEnchant(enchant as EnchantmentWrapper)
            }
            broadcast(enchant)
            broadcast(enchantLevel)
            broadcast(newLevel)
            if (anvil.firstItem?.type == Material.ENCHANTED_BOOK) {
                broadcast("book")
                anvil.result = anvil.firstItem
                (anvil.result?.itemMeta as EnchantmentStorageMeta).addStoredEnchant(enchant, enchantLevel, true)
                anvil.result?.updateEnchantmentLore(enchant as EnchantmentWrapper, newLevel)
            }
            if (anvil.firstItem?.type != Material.ENCHANTED_BOOK) {
                broadcast("not book")
                anvil.firstItem?.addEnchantment(enchant as EnchantmentWrapper, newLevel)
                anvil.result = anvil.firstItem
            }
        }
    }

    @EventHandler
    fun InventoryClickEvent.removeCustomEnchantGrindstone() {
        if (inventory.type != InventoryType.GRINDSTONE) return
        val grindstone = inventory as GrindstoneInventory
        val item =
        if (grindstone.upperItem != null) {
            if (grindstone.upperItem?.type == Material.ENCHANTED_BOOK) (grindstone.upperItem?.itemMeta as EnchantmentStorageMeta).storedEnchants
            else grindstone.upperItem?.enchantments
        }
        else if (grindstone.lowerItem != null) {
            if (grindstone.lowerItem?.type == Material.ENCHANTED_BOOK) (grindstone.lowerItem?.itemMeta as EnchantmentStorageMeta).storedEnchants
            else grindstone.lowerItem?.enchantments
        }
        else return

        item!!.forEach {
            grindstone.upperItem?.removeEnchantment(it as Enchantment)
            grindstone.lowerItem?.removeCustomEnchant(it as Enchantment)
        }

    }
}