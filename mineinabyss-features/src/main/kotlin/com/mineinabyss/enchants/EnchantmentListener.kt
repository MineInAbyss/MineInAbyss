package com.mineinabyss.enchants

import com.mineinabyss.components.enchantments.Enchantment
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.looty.tracking.toGearyFromUUIDOrNull
import org.bukkit.Material
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.GrindstoneInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.SmithingInventory

class EnchantmentListener : Listener {

    @EventHandler
    fun InventoryOpenEvent.enchantWithAnvil() {

        if (inventory.type == InventoryType.ANVIL) {
            val inv = inventory as AnvilInventory
            val book = inv.secondItem?.toGearyFromUUIDOrNull() ?: return
            val resultItem = inv.firstItem
            val enchant = book.get<Enchantment>()?.enchantment ?: return
            val level = book.get<Enchantment>()?.level ?: return

            if (inv.secondItem!!.type != Material.ENCHANTED_BOOK) return

            enchant.forEach {
                if (!CustomEnchants.enchantmentList.contains(it)) return@forEach
                if (it.itemTarget != getItemTarget(inv.firstItem)) return@forEach
                resultItem?.addEnchantment(it, level)
            }
            broadcast(enchant)

            inv.result = resultItem
            return
        }

        if (inventory.type == InventoryType.GRINDSTONE) {
            //TODO Implement grindstone feature
            val inv = inventory as GrindstoneInventory

            return
        }

        if (inventory.type == InventoryType.SMITHING) {
            //TODO Implement Smithing feature
            val inv = inventory as SmithingInventory

            return
        }
    }

    private fun getItemTarget(itemStack: ItemStack?): EnchantmentTarget? {
        if (itemStack == null) return null
        when {
            EnchantmentTarget.ARMOR.includes(itemStack) -> return EnchantmentTarget.ARMOR
            EnchantmentTarget.ARMOR_FEET.includes(itemStack) -> return EnchantmentTarget.ARMOR_FEET
            EnchantmentTarget.ARMOR_LEGS.includes(itemStack) -> return EnchantmentTarget.ARMOR_LEGS
            EnchantmentTarget.ARMOR_TORSO.includes(itemStack) -> return EnchantmentTarget.ARMOR_TORSO
            EnchantmentTarget.ARMOR_HEAD.includes(itemStack) -> return EnchantmentTarget.ARMOR_HEAD
            EnchantmentTarget.WEAPON.includes(itemStack) -> return EnchantmentTarget.WEAPON
            EnchantmentTarget.TOOL.includes(itemStack) -> return EnchantmentTarget.TOOL
            EnchantmentTarget.BOW.includes(itemStack) -> return EnchantmentTarget.BOW
            EnchantmentTarget.FISHING_ROD.includes(itemStack) -> return EnchantmentTarget.FISHING_ROD
            EnchantmentTarget.BREAKABLE.includes(itemStack) -> return EnchantmentTarget.BREAKABLE
            EnchantmentTarget.WEARABLE.includes(itemStack) -> return EnchantmentTarget.WEARABLE
            EnchantmentTarget.TRIDENT.includes(itemStack) -> return EnchantmentTarget.TRIDENT
            EnchantmentTarget.CROSSBOW.includes(itemStack) -> return EnchantmentTarget.CROSSBOW
            EnchantmentTarget.VANISHABLE.includes(itemStack) -> return EnchantmentTarget.VANISHABLE
            else -> return null
        }
    }
}