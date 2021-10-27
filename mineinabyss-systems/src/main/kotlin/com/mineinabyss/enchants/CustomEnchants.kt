package com.mineinabyss.enchants

import com.mineinabyss.idofront.messaging.logInfo
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.join
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Field

object CustomEnchants {
    val enchantmentList = mutableListOf<Enchantment>()
    val SOULBOUND = EnchantmentWrapper("soulbound", "Soulbound", 1, EnchantmentTarget.ALL, color(150, 10, 10))
    val FROST_ASPECT =
        EnchantmentWrapper("frostaspect", "Frost Aspect", 2, EnchantmentTarget.WEAPON, color(0, 100, 220))
    val BIRD_SWATTER = EnchantmentWrapper("birdswatter", "Bird Swatter", 5, EnchantmentTarget.WEAPON, color(0,200,60))
    val JAW_BREAKER = EnchantmentWrapper("jawbreaker", "Jaw Breaker", 3, EnchantmentTarget.WEAPON, color(150,20,150))
    val BANE_OF_KUONGATARI = EnchantmentWrapper("baneofkuongatari", "Bane of Kuongatari", 1, EnchantmentTarget.WEAPON, color(0,200,80))


    fun register() {
        if (!Enchantment.values().contains(SOULBOUND)) registerEnchantment(SOULBOUND)
        if (!Enchantment.values().contains(FROST_ASPECT)) registerEnchantment(FROST_ASPECT)
        if (!Enchantment.values().contains(BIRD_SWATTER)) registerEnchantment(BIRD_SWATTER)
        if (!Enchantment.values().contains(JAW_BREAKER)) registerEnchantment(JAW_BREAKER)
        if (!Enchantment.values().contains(BANE_OF_KUONGATARI)) registerEnchantment(BANE_OF_KUONGATARI)

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
            enchantmentList.add(enchantment)
            logInfo("Enchantment Registered")
        }
    }

}

fun ItemStack.addCustomEnchant(enchantment: EnchantmentWrapper, lvl: Int, extraLore: String = "") {
    removeCustomEnchant(enchantment)
    updateEnchantmentLore(enchantment, lvl, extraLore)
    addEnchantment(enchantment, lvl)
    updateEnchantmentLore(enchantment, lvl, extraLore)
}

// Lore is not removed correctly
fun ItemStack.removeCustomEnchant(enchantment: Enchantment) {
    val lore: MutableList<Component> = lore() ?: mutableListOf()
    lore.removeIf { it.contains(enchantment.displayName(getEnchantmentLevel(enchantment))) }
    removeEnchantment(enchantment)
}

fun ItemStack.updateEnchantmentLore(enchantment: EnchantmentWrapper, lvl: Int, extraLore: String = "") {
    val lore: MutableList<Component> = lore() ?: mutableListOf()

    val check = lore.firstOrNull {
        PlainTextComponentSerializer.plainText().serialize(it) ==
                PlainTextComponentSerializer.plainText().serialize(
                    join(
                        JoinConfiguration.separator(Component.space()),
                        enchantment.displayName(lvl),
                        Component.text(extraLore).color(enchantment.loreColor).decoration(
                            TextDecoration.ITALIC, false
                        )
                    )
                )
    }

    if (check == null) {
        lore.add(
            0, join(
                JoinConfiguration.separator(Component.space()),
                enchantment.displayName(lvl),
                Component.text(extraLore).color(enchantment.loreColor).decoration(
                    TextDecoration.ITALIC, false
                )
            )
        )
        lore(lore)
    }
}


