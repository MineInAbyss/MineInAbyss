package com.mineinabyss.enchants

import com.mineinabyss.idofront.messaging.error
import org.bukkit.Material
import org.bukkit.enchantments.EnchantmentTarget
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

        // Up dumb limit of vanilla
        anvil.maximumRepairCost = 100
        anvil.repairCost = enchanted?.let { calculateItemEnchantCost(it) }!!

        enchanted.forEach {
            val target = getItemTarget(anvil.firstItem)
            val enchant = it.key
            val enchantLevel = it.value
            var newLevel: Int

            if (it.key.itemTarget != target && slot == 2 && anvil.firstItem?.type != Material.ENCHANTED_BOOK && target != EnchantmentTarget.ALL) {
                player.error("This book cannot be added to this item")
                anvil.maximumRepairCost = 0
                isCancelled = true
                player.closeInventory()
                return@forEach
            }

            val first = anvil.firstItem
            val itemLevel =
                if (first?.type != Material.ENCHANTED_BOOK && first?.containsEnchantment(enchant) == true) {
                    first.getEnchantmentLevel(enchant)
                }
                else if (first?.type == Material.ENCHANTED_BOOK && (first.itemMeta as EnchantmentStorageMeta).hasStoredEnchant(enchant)) {
                    (first.itemMeta as EnchantmentStorageMeta).storedEnchants[enchant]!!
                }
                else 0

            newLevel = when {
                itemLevel == enchantLevel -> itemLevel + 1
                itemLevel > enchantLevel -> itemLevel
                itemLevel <= enchantLevel -> enchantLevel
                else -> itemLevel
            }

            if (newLevel > enchant.maxLevel) newLevel = enchant.maxLevel

            if (anvil.firstItem?.type != Material.ENCHANTED_BOOK) {
                anvil.result = anvil.firstItem
                anvil.result?.addCustomEnchant(enchant as EnchantmentWrapper, newLevel)
                anvil.result?.updateEnchantmentLore(enchant as EnchantmentWrapper, itemLevel, "", true)

            } else if (anvil.firstItem?.type == Material.ENCHANTED_BOOK) {
                anvil.result = anvil.firstItem
                val bookMeta = (anvil.result?.itemMeta as EnchantmentStorageMeta)

                bookMeta.addStoredEnchant(enchant, newLevel, false)
                anvil.result?.itemMeta = bookMeta
                anvil.result?.updateEnchantmentLore(enchant as EnchantmentWrapper, itemLevel, removeLore = true)
                anvil.result?.updateEnchantmentLore(enchant as EnchantmentWrapper, newLevel)
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
            if (CustomEnchants.enchantmentList.contains(it.key))
                grindstone.result?.removeCustomEnchant(it.key as EnchantmentWrapper)
            else if (!CustomEnchants.enchantmentList.contains(it.key))
                grindstone.result?.removeEnchantment(it.key)
        }
    }
}