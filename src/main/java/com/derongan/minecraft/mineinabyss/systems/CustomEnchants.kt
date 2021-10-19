package com.derongan.minecraft.mineinabyss.systems

import com.mineinabyss.idofront.messaging.logInfo
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor.color
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Field

object CustomEnchants {
    val SOULBOUND = EnchantmentWrapper("soulbound", "Soulbound", 1, color(150, 10, 10))

    fun register() {
        val registered: Boolean = Enchantment.values().contains(SOULBOUND)
        if (!registered) registerEnchantment(SOULBOUND)
    }

    private fun registerEnchantment(enchantment: Enchantment) {
        var registered = true
        try {
            val field: Field = Enchantment::class.java.getDeclaredField("acceptingNew")
            field.isAccessible = true
            field.set(null, true)
            Enchantment.registerEnchantment(enchantment)
        } catch (e: Exception) {
            registered = false
            e.printStackTrace()
        }
        if (registered) {
            logInfo("Enchantment Registered")
        }
    }

}

fun ItemStack.addCustomEnchant(enchantment: CustomEnchantment, lvl: Int, extraLore: Component = Component.text("")) {
    addEnchantment(enchantment, lvl)
    val lore: MutableList<Component> = lore() ?: mutableListOf()
    lore.add(0, enchantment.displayName(lvl).append(extraLore))
    lore(lore)
}

fun ItemStack.removeCustomEnchant(enchantment: Enchantment) {
    val lore: MutableList<Component> = lore() ?: mutableListOf()
    lore.removeIf { it.contains(enchantment.displayName(getEnchantmentLevel(enchantment))) }
    removeEnchantment(enchantment)
}