package com.mineinabyss.features.helpers

import androidx.compose.runtime.Composable
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.ProfileManager
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.datacomponent.item.ResolvableProfile
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack

object TitleItem {
    fun of(name: String, vararg lore: String) = ItemStack.of(Material.PAPER).apply {
        setData(DataComponentTypes.ITEM_NAME, name.miniMsg())
        setData(DataComponentTypes.LORE, ItemLore.lore(lore.map { it.miniMsg() }))
        setData(DataComponentTypes.ITEM_MODEL, Key.key("minecraft:empty"))
    }
    fun of(name: Component, vararg lore: Component) = ItemStack.of(Material.PAPER).apply {
        setData(DataComponentTypes.ITEM_NAME, name)
        setData(DataComponentTypes.LORE, ItemLore.lore(lore.toList()))
        setData(DataComponentTypes.ITEM_MODEL, Key.key("minecraft:empty"))
    }

    val transparentItem = ItemStack.of(Material.PAPER).apply {
        setData(DataComponentTypes.ITEM_MODEL, Key.key("minecraft:empty"))
        setData(DataComponentTypes.HIDE_TOOLTIP)
    }

    fun head(
        player: OfflinePlayer,
        title: Component,
        vararg lore: Component,
        isFlat: Boolean = false,
        isLarge: Boolean = false,
        isCenterOfInv: Boolean = false,
    ): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD).apply {
            setData(DataComponentTypes.ITEM_NAME, title)
            setData(DataComponentTypes.LORE, ItemLore.lore(lore.toList()))
            setData(DataComponentTypes.ITEM_MODEL, Key.key("mineinabyss:head"))

            if (!isFlat && !isLarge && !isCenterOfInv) return@apply
            val cmd = CustomModelData.customModelData().addFloat(when {
                isCenterOfInv -> if (isLarge) 4f else 3f
                else -> if (isLarge) 2f else 1f
            }).build()
            setData(DataComponentTypes.CUSTOM_MODEL_DATA, cmd)
        }

        abyss.plugin.launch(abyss.plugin.asyncDispatcher) {
            val profile = when {
                player.isOnline -> player.playerProfile
                player.uniqueId in ProfileManager.profileCache -> ProfileManager.profileCache[player.uniqueId]!!
                else -> ProfileManager.getOrRequestProfile(player.uniqueId)
            }
            ProfileManager.profileCache[player.uniqueId] = profile

            item.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(profile))
        }

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
