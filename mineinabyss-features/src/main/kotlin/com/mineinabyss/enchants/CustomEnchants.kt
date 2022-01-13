package com.mineinabyss.enchants

import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.logInfo
import io.papermc.paper.enchantments.EnchantmentRarity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import java.lang.reflect.Field

@Serializable
@SerialName("mineinabyss:customenchant")
object CustomEnchants {
    val enchantmentList = mutableListOf<Enchantment>()
    val SOULBOUND = EnchantmentWrapper("soulbound", "Soulbound", 1, listOf(CustomEnchantTargets.ALL), listOf(Enchantment.BINDING_CURSE, Enchantment.VANISHING_CURSE), EnchantmentRarity.RARE, color(150, 10, 10))
    val FROST_ASPECT = EnchantmentWrapper("frostaspect", "Frost Aspect", 2, listOf(CustomEnchantTargets.SWORD), listOf(Enchantment.FIRE_ASPECT), EnchantmentRarity.COMMON, color(0, 100, 220))
    val BIRD_SWATTER = EnchantmentWrapper("birdswatter", "Bird Swatter", 5, listOf(CustomEnchantTargets.SWORD), listOf(Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_ARTHROPODS, Enchantment.DAMAGE_UNDEAD), EnchantmentRarity.COMMON, color(0,220,60))
    val JAW_BREAKER = EnchantmentWrapper("jawbreaker", "Jaw Breaker", 3, listOf(CustomEnchantTargets.SWORD), listOf(Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_ARTHROPODS, Enchantment.DAMAGE_UNDEAD, BIRD_SWATTER), EnchantmentRarity.COMMON, color(150,20,150))
    val BANE_OF_KUONGATARI = EnchantmentWrapper("baneofkuongatari", "Bane of Kuongatari", 4, listOf(CustomEnchantTargets.SWORD), listOf(Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_ARTHROPODS, Enchantment.DAMAGE_UNDEAD, BIRD_SWATTER, JAW_BREAKER), EnchantmentRarity.COMMON, color(0,200,80))


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
    addEnchantment(enchantment, lvl)
    updateEnchantmentLore(enchantment, lvl, extraLore)
}

fun ItemStack.updateEnchantmentLore(enchantment: EnchantmentWrapper, lvl: Int, extraLore: String = "", removeLore: Boolean = false) {
    val lore: MutableList<Component> = lore() ?: mutableListOf()
    val enchantName = Component.text(enchantment.name)
    val enchantLevel = if (enchantment.maxLevel > enchantment.startLevel)Component.text(convertEnchantmentLevel(lvl)) else Component.empty()
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

fun getItemTarget(itemStack: ItemStack?): CustomEnchantTargets? {
    if (itemStack == null) return null
    when {
        CustomEnchantTargets.ARMOR.includes(itemStack) -> return CustomEnchantTargets.ARMOR
        CustomEnchantTargets.ARMOR_FEET.includes(itemStack) -> return CustomEnchantTargets.ARMOR_FEET
        CustomEnchantTargets.ARMOR_LEGS.includes(itemStack) -> return CustomEnchantTargets.ARMOR_LEGS
        CustomEnchantTargets.ARMOR_TORSO.includes(itemStack) -> return CustomEnchantTargets.ARMOR_TORSO
        CustomEnchantTargets.ARMOR_HEAD.includes(itemStack) -> return CustomEnchantTargets.ARMOR_HEAD
        CustomEnchantTargets.SWORD.includes(itemStack) -> return CustomEnchantTargets.SWORD
        CustomEnchantTargets.TOOL.includes(itemStack) -> return CustomEnchantTargets.TOOL
        CustomEnchantTargets.BOW.includes(itemStack) -> return CustomEnchantTargets.BOW
        CustomEnchantTargets.FISHING_ROD.includes(itemStack) -> return CustomEnchantTargets.FISHING_ROD
        CustomEnchantTargets.BREAKABLE.includes(itemStack) -> return CustomEnchantTargets.BREAKABLE
        CustomEnchantTargets.WEARABLE.includes(itemStack) -> return CustomEnchantTargets.WEARABLE
        CustomEnchantTargets.TRIDENT.includes(itemStack) -> return CustomEnchantTargets.TRIDENT
        CustomEnchantTargets.CROSSBOW.includes(itemStack) -> return CustomEnchantTargets.CROSSBOW
        CustomEnchantTargets.VANISHABLE.includes(itemStack) -> return CustomEnchantTargets.VANISHABLE
        else -> return CustomEnchantTargets.ALL
    }
}

fun getEnchantmentTarget(enchantment: Enchantment) : List<CustomEnchantTargets> {
    // TODO Make it so enchantment can identify custom target
    val targetList = mutableListOf<CustomEnchantTargets>()

    if (CustomEnchants.enchantmentList.contains(enchantment)) return targetList
    when (enchantment.itemTarget) {
        EnchantmentTarget.WEAPON -> targetList += CustomEnchantTargets.SWORD
        EnchantmentTarget.ARMOR -> targetList += CustomEnchantTargets.ARMOR
        EnchantmentTarget.ARMOR_FEET -> targetList += CustomEnchantTargets.ARMOR_FEET
        EnchantmentTarget.ARMOR_LEGS -> targetList += CustomEnchantTargets.ARMOR_LEGS
        EnchantmentTarget.ARMOR_TORSO -> targetList += CustomEnchantTargets.ARMOR_TORSO
        EnchantmentTarget.ARMOR_HEAD -> targetList += CustomEnchantTargets.ARMOR_HEAD
        EnchantmentTarget.BOW -> targetList += CustomEnchantTargets.BOW
        EnchantmentTarget.BREAKABLE -> targetList += CustomEnchantTargets.BREAKABLE
        EnchantmentTarget.TRIDENT -> targetList += CustomEnchantTargets.TRIDENT
        EnchantmentTarget.CROSSBOW -> targetList += CustomEnchantTargets.CROSSBOW
        EnchantmentTarget.FISHING_ROD -> targetList += CustomEnchantTargets.FISHING_ROD
        EnchantmentTarget.TOOL -> targetList += CustomEnchantTargets.TOOL
        EnchantmentTarget.VANISHABLE -> targetList += CustomEnchantTargets.VANISHABLE
        EnchantmentTarget.WEARABLE -> targetList += CustomEnchantTargets.WEARABLE
        EnchantmentTarget.ALL -> targetList += CustomEnchantTargets.ALL
        else -> return targetList
    }

    return targetList
}

fun checkIncompatibleEnchants(firstItem: ItemStack?, secondItem: ItemStack?): List<Enchantment?> {
    val conflictList: MutableList<Enchantment> = mutableListOf()

    if (firstItem != null && secondItem != null) {
        if (firstItem.enchantments.isEmpty() ||
            secondItem.enchantments.isEmpty() ||
            !(firstItem.itemMeta as EnchantmentStorageMeta).hasStoredEnchants() ||
            !(secondItem.itemMeta as EnchantmentStorageMeta).hasStoredEnchants()
        ) return conflictList

        if (firstItem.type == Material.ENCHANTED_BOOK) {
            broadcast("book")
            (firstItem.itemMeta as EnchantmentStorageMeta).storedEnchants.forEach { firstEnchant ->
                (secondItem.itemMeta as EnchantmentStorageMeta).storedEnchants.forEach { secondEnchant ->
                    if (secondEnchant.key.conflictsWith(firstEnchant.key) || firstEnchant.key.conflictsWith(
                            secondEnchant.key
                        )
                    ) {
                        conflictList += secondEnchant.key
                    }
                }
            }
        }
        if (firstItem.type != Material.ENCHANTED_BOOK) {
            broadcast("item")
            firstItem.enchantments.forEach { firstEnchant ->
                secondItem.enchantments.forEach { secondEnchant ->
                    broadcast(firstEnchant.key)
                    broadcast(secondEnchant.key)
                    // Due to registering conflicting custom enchants onto vanilla being weird
                    if (secondEnchant.key.conflictsWith(firstEnchant.key) || firstEnchant.key.conflictsWith(secondEnchant.key)) {
                        conflictList += secondEnchant.key
                    }
                }
            }
        }
    }
    return conflictList
}

fun convertEnchantmentLevel(level: Int) : String {
    val newLevel = when (level) {
        0 -> ""
        1 -> "I"
        2 -> "II"
        3 -> "III"
        4 -> "IV"
        5 -> "V"
        6 -> "VI"
        7 -> "VII"
        8 -> "VIII"
        9 -> "IX"
        10 -> "X"
        20 -> "XX"
        50 -> "L"
        100 -> "C"

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
        cost += (defaultCost * it.value).toInt()
    }
    return cost
}
