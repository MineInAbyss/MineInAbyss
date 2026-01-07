package com.mineinabyss.features.helpers

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.idofront.resourcepacks.ResourcePacks
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.datacomponent.item.ResolvableProfile
import io.papermc.paper.datacomponent.item.TooltipDisplay
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack

object TitleItem {
    val hideTooltip = TooltipDisplay.tooltipDisplay().hideTooltip(true)
    val headItemModel = Key.key("mineinabyss:ui/head")

    fun of(name: String, vararg lore: String) = ItemStack.of(Material.PAPER).apply {
        setData(DataComponentTypes.ITEM_NAME, name.miniMsg())
        setData(DataComponentTypes.LORE, ItemLore.lore(lore.map { it.miniMsg() }))
        setData(DataComponentTypes.ITEM_MODEL, ResourcePacks.EMPTY_MODEL)
    }
    fun of(name: Component, vararg lore: Component) = ItemStack.of(Material.PAPER).apply {
        setData(DataComponentTypes.ITEM_NAME, name)
        setData(DataComponentTypes.LORE, ItemLore.lore(lore.toList()))
        setData(DataComponentTypes.ITEM_MODEL, ResourcePacks.EMPTY_MODEL)
    }

    val transparentItem = ItemStack.of(Material.PAPER).apply {
        setData(DataComponentTypes.ITEM_MODEL, ResourcePacks.EMPTY_MODEL)
        setData(DataComponentTypes.TOOLTIP_DISPLAY, hideTooltip)
    }

    fun head(
        player: OfflinePlayer,
        title: Component,
        vararg lore: Component,
        isFlat: Boolean = false,
        isLarge: Boolean = false,
        isCenterOfInv: Boolean = false,
    ): ItemStack {
        val item = ItemStack.of(Material.PLAYER_HEAD)
        item.setData(DataComponentTypes.ITEM_NAME, title)
        item.setData(DataComponentTypes.LORE, ItemLore.lore(lore.toList()))
        item.setData(DataComponentTypes.ITEM_MODEL, headItemModel)
        item.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile().uuid(player.uniqueId).build())
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.PROFILE).build())
        item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addFlags(listOf(isFlat, isLarge, isCenterOfInv)).build())

        return item
    }
}

@Composable
fun Text(name: String, vararg lore: String, modifier: Modifier = Modifier) {
    Item(TitleItem.of(name, *lore), modifier)
}

@Composable
fun Text(name: Component, vararg lore: Component, modifier: Modifier = Modifier) {
    Item(TitleItem.of(name, *lore), modifier)
}
