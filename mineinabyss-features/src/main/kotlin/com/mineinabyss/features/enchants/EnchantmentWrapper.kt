package com.mineinabyss.features.enchants

import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.world.item.enchantment.EnchantmentCategory
import net.minecraft.network.chat.Component as NMSComponent
import net.minecraft.world.item.enchantment.Enchantment as NMSEnchantment


//class EnchantmentWrapper(
//    private val namespace: String,
//    private val name: String,
//    private val maxLvl: Int,
//    allowedItems: EnchantmentCategory,
//    rarity: Rarity,
//    val loreColor: TextColor = color(150, 10, 10),
//    vararg slotTypes: net.minecraft.world.entity.EquipmentSlot,
//) : NMSEnchantment(rarity, allowedItems, slotTypes) {
//
//    override fun getFullname(level: Int): NMSComponent {
//        val component = Component.text(name).color(loreColor).decoration(TextDecoration.ITALIC, false)
//
//        if (level != maxLvl) {
//            component.append(Component.text(" $level"))
//        }
//        return PaperAdventure.asVanilla(component)
//    }
//
//    override fun getMaxLevel(): Int = maxLvl
//}
