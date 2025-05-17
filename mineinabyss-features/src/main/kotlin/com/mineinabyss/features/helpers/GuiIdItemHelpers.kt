package com.mineinabyss.features.helpers

import androidx.compose.runtime.Composable
import com.destroystokyo.paper.profile.PlayerProfile
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.idofront.items.editItemMeta
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
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

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

    internal val profileCache: MutableMap<UUID, PlayerProfile> = mutableMapOf()
    fun head(
        player: OfflinePlayer,
        title: Component,
        vararg lore: Component,
        isFlat: Boolean = false,
        isLarge: Boolean = false,
        isCenterOfInv: Boolean = false,
    ): ItemStack {
        return ItemStack(Material.PLAYER_HEAD).apply {
            setData(DataComponentTypes.ITEM_NAME, title)
            setData(DataComponentTypes.LORE, ItemLore.lore(lore.toList()))
            setData(DataComponentTypes.ITEM_MODEL, Key.key("mineinabyss:head"))

            if (!isFlat && !isLarge && !isCenterOfInv) return@apply
            val cmd = CustomModelData.customModelData().addFloat(when {
                isCenterOfInv -> if (isLarge) 4f else 3f
                else -> if (isLarge) 2f else 1f
            }).build()
            setData(DataComponentTypes.CUSTOM_MODEL_DATA, cmd)
        }.also { item ->
            when {
                player.isOnline -> {
                    val profile = player.playerProfile
                    item.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(profile))
                    profileCache[player.uniqueId] = profile
                }

                player.uniqueId in profileCache -> item.editItemMeta<SkullMeta> { playerProfile = profileCache[player.uniqueId] }

                else -> player.playerProfile.update().whenCompleteAsync { profile, _ ->
                    profileCache[player.uniqueId] = profile
                    item.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(profile))
                }
            }
        }
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
