package com.derongan.minecraft.mineinabyss.systems

import io.papermc.paper.enchantments.EnchantmentRarity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.entity.EntityCategory
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

abstract class CustomEnchantment(namespace: String) : Enchantment(NamespacedKey.minecraft(namespace))

class EnchantmentWrapper(
    private val namespace: String,
    private val name: String,
    private val maxLvl: Int,
    val loreColor: TextColor = color(150, 10, 10)
) : CustomEnchantment(namespace) {
    override fun canEnchantItem(item: ItemStack): Boolean {
        return true
    }

    override fun getName(): String {
        return name
    }

    override fun displayName(level: Int): Component {
        val component = Component.text(name).color(loreColor).decoration(TextDecoration.ITALIC, false)

        if (level != maxLvl) {
            component.append(Component.text(" $level"))
        }

        return component
    }

    override fun translationKey(): String {
        return "enchantment.$namespace"
    }

    override fun getStartLevel(): Int {
        return 1
    }

    override fun getMaxLevel(): Int {
        return maxLvl
    }

    override fun isTradeable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isDiscoverable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getRarity(): EnchantmentRarity {
        TODO("Not yet implemented")
    }

    override fun getDamageIncrease(level: Int, entityCategory: EntityCategory): Float {
        TODO("Not yet implemented")
    }

    override fun getActiveSlots(): MutableSet<EquipmentSlot> {
        TODO("Not yet implemented")
    }

    override fun getItemTarget(): EnchantmentTarget {
        TODO("Not yet implemented")
    }

    override fun isTreasure(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isCursed(): Boolean {
        TODO("Not yet implemented")
    }

    override fun conflictsWith(other: Enchantment): Boolean {
        TODO("Not yet implemented")
    }
}