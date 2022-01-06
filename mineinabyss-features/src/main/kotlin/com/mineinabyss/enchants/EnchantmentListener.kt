package com.mineinabyss.enchants

import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.messaging.error
import org.bukkit.Material
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

        val player = whoClicked as Player
        val enchanted =
            if (anvil.secondItem?.type == Material.ENCHANTED_BOOK)
                    (anvil.secondItem?.itemMeta as EnchantmentStorageMeta).storedEnchants
            else if (anvil.secondItem?.type != Material.ENCHANTED_BOOK && anvil.secondItem != null)
                anvil.secondItem?.enchantments
            else return

        if (anvil.firstItem?.type != anvil.secondItem?.type && anvil.secondItem?.type != Material.ENCHANTED_BOOK) return

        enchanted?.forEach {
            val target = getItemTarget(anvil.firstItem)
            val enchant = it.component1()
            val enchantLevel = it.component2()
            var newLevel = enchantLevel

            if (it.component1().itemTarget != target && slot == 2 && anvil.firstItem?.type != Material.ENCHANTED_BOOK) {
                player.error("This book cannot be added to this item")
                anvil.maximumRepairCost = 0
                isCancelled = true
                player.closeInventory()
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
                    else -> itemLevel
                }
                if (newLevel > enchant.maxLevel) newLevel = enchant.maxLevel
                anvil.firstItem?.removeCustomEnchant(enchant as EnchantmentWrapper)
            }

            if (anvil.firstItem?.type != Material.ENCHANTED_BOOK) {
                anvil.result = anvil.firstItem
                anvil.result?.addCustomEnchant(enchant as EnchantmentWrapper, newLevel)
            }

            else if (anvil.firstItem?.type == Material.ENCHANTED_BOOK) {
                anvil.result = anvil.firstItem
                val bookMeta = (anvil.result?.itemMeta as EnchantmentStorageMeta)

                bookMeta.addStoredEnchant(enchant, newLevel, false)
                anvil.result?.itemMeta = bookMeta
                anvil.result?.updateEnchantmentLore(enchant as EnchantmentWrapper, newLevel, "")
            }
        }
    }

    @EventHandler
    fun InventoryClickEvent.removeCustomEnchantGrindstone() {
        if (inventory.type != InventoryType.GRINDSTONE) return
        if (slot != 2) return
        val grindstone = inventory as GrindstoneInventory
        val enchant =
        if (grindstone.upperItem != null) {
            grindstone.result?.type = grindstone.upperItem?.type!!
            grindstone.result?.itemMeta = grindstone.upperItem?.itemMeta

            if (grindstone.upperItem?.type == Material.ENCHANTED_BOOK)
                    (grindstone.upperItem?.itemMeta as EnchantmentStorageMeta).storedEnchants
            else
                grindstone.upperItem?.enchantments
        }
        else if (grindstone.lowerItem != null) {
            if (grindstone.upperItem == null) {
                grindstone.result?.type = grindstone.lowerItem?.type!!
                grindstone.result?.itemMeta = grindstone.lowerItem?.itemMeta
            }
            if (grindstone.lowerItem?.type == Material.ENCHANTED_BOOK)
                    (grindstone.lowerItem?.itemMeta as EnchantmentStorageMeta).storedEnchants
            else
                grindstone.lowerItem?.enchantments
        }
        else return

        enchant?.forEach {
            if (CustomEnchants.enchantmentList.contains(it.component1()))
                grindstone.result?.removeCustomEnchant(it.component1() as EnchantmentWrapper)
            else if (!CustomEnchants.enchantmentList.contains(it.component1()))
                grindstone.result?.removeEnchantment(it.component1())
        }
    }
}