package com.mineinabyss.enchants

import com.mineinabyss.idofront.messaging.logInfo
import io.papermc.paper.enchantments.EnchantmentRarity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Field

@Serializable
@SerialName("mineinabyss:customenchant")
object CustomEnchants {
    val enchantmentList = mutableListOf<Enchantment>()
    val SOULBOUND = EnchantmentWrapper("soulbound", "Soulbound", 1, EnchantmentTarget.ALL, listOf(Enchantment.BINDING_CURSE, Enchantment.VANISHING_CURSE), EnchantmentRarity.RARE, color(150, 10, 10))
    val FROST_ASPECT = EnchantmentWrapper("frostaspect", "Frost Aspect", 2, EnchantmentTarget.WEAPON, listOf(Enchantment.FIRE_ASPECT), EnchantmentRarity.COMMON, color(0, 100, 220))
    val BIRD_SWATTER = EnchantmentWrapper("birdswatter", "Bird Swatter", 5, EnchantmentTarget.WEAPON, listOf(), EnchantmentRarity.COMMON, color(0,220,60))
    val JAW_BREAKER = EnchantmentWrapper("jawbreaker", "Jaw Breaker", 3, EnchantmentTarget.WEAPON, listOf(BIRD_SWATTER), EnchantmentRarity.COMMON, color(150,20,150))
    val BANE_OF_KUONGATARI = EnchantmentWrapper("baneofkuongatari", "Bane of Kuongatari", 1, EnchantmentTarget.WEAPON, listOf(), EnchantmentRarity.COMMON, color(0,200,80))


    fun register() {
        val registered: Boolean = Enchantment.values().contains(SOULBOUND)
        if (!registered) registerEnchantment(SOULBOUND)

        val registered2: Boolean = Enchantment.values().contains(FROST_ASPECT)
        if (!registered2) registerEnchantment(FROST_ASPECT)
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
    addEnchantment(enchantment, lvl)
    updateEnchantmentLore(enchantment, lvl, extraLore)
}

fun ItemStack.updateEnchantmentLore(enchantment: EnchantmentWrapper, lvl: Int, extraLore: String = "", removeLore: Boolean = false) {
    val lore: MutableList<Component> = lore() ?: mutableListOf()
    val enchantName = Component.text(enchantment.name)
    val enchantLevel = Component.text(convertEnchantmentLevel(lvl))
    val moreLore = Component.text(extraLore)
    val loreComponent = enchantName.append(Component.space()).append(enchantLevel).append(Component.space()).append(moreLore).color(enchantment.loreColor).decoration(TextDecoration.ITALIC, false)

    if (removeLore) {
        if (lore.contains(loreComponent)) lore.remove(loreComponent)
        lore(lore)
        return
    }

    val check = lore.firstOrNull {
        PlainTextComponentSerializer.plainText().serialize(it) ==
                PlainTextComponentSerializer.plainText().serialize(loreComponent)
    }

    if (check == null) {
        lore.add(0, loreComponent.color(enchantment.loreColor).decoration(TextDecoration.ITALIC, false))
        lore(lore)
    }
}

fun ItemStack.removeCustomEnchant(enchantment: EnchantmentWrapper) {
    updateEnchantmentLore(enchantment, getEnchantmentLevel(enchantment), removeLore = true)
    removeEnchantment(enchantment)
}

fun getItemTarget(itemStack: ItemStack?): EnchantmentTarget? {
    if (itemStack == null) return null
    when {
        EnchantmentTarget.ALL.includes(itemStack) -> return EnchantmentTarget.ALL
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

fun convertEnchantmentLevel(level: Int) : String {
    val newLevel = when (level) {
        0 -> ""
        1 -> "I"
        2 -> "II"
        3 -> "III"
        4 -> "IV"
        5 -> "V"
        else -> ""
    }

    return newLevel
}

fun calculateItemEnchantCost(enchants: MutableMap<Enchantment, Int>): Int {
    var cost = 0
    enchants.forEach {
        val range: IntRange = it.key.startLevel..it.key.maxLevel
        val rarity = it.key.rarity
        var defaultCost: Double =
            (if (range.last.mod(2) == 0) 2
            else if (range.last == 1) 2
            else if (range.last.mod(3) == 0) 2
            else if (range.last.mod(5) == 0) 1
            else 2).toDouble()

        when {
            rarity == EnchantmentRarity.COMMON -> defaultCost *= 1
            rarity == EnchantmentRarity.UNCOMMON -> defaultCost *= 1.25
            rarity == EnchantmentRarity.RARE -> defaultCost *= 1.5
            rarity == EnchantmentRarity.VERY_RARE -> defaultCost *= 2
            else -> defaultCost *= 1
        }
        cost = (defaultCost * it.value).toInt()
    }
    return cost
}
