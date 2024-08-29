package com.mineinabyss.features.helpers

import androidx.compose.runtime.Composable
import com.destroystokyo.paper.profile.PlayerProfile
import com.mineinabyss.features.abyss
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

object TitleItem {
    fun of(name: String, vararg lore: String) = ItemStack.of(Material.PAPER).editItemMeta {
        itemName(name.miniMsg())
        lore(lore.toList().map { it.miniMsg() })
        setCustomModelData(1)
    }
    fun of(name: Component, vararg lore: Component) = ItemStack.of(Material.PAPER).editItemMeta {
        itemName(name)
        lore(lore.toList())
        setCustomModelData(1)
    }

    val transparentItem = ItemStack.of(Material.PAPER).editItemMeta {
        setCustomModelData(1)
        isHideTooltip = true
    }

    private val profileCache: MutableMap<UUID, PlayerProfile> = mutableMapOf()
    fun head(
        player: OfflinePlayer,
        title: Component,
        vararg lore: Component,
        isFlat: Boolean = false,
        isLarge: Boolean = false,
        isCenterOfInv: Boolean = false,
    ): ItemStack {
        return ItemStack(Material.PLAYER_HEAD).editItemMeta<SkullMeta> {
            itemName(title)
            lore(lore.toList())
            if (isFlat) setCustomModelData(1)
            if (isLarge) setCustomModelData(2)
            if (isCenterOfInv && !isLarge) setCustomModelData(3)
            if (isCenterOfInv && isLarge) setCustomModelData(4)
        }.also { item ->
            when {
                player.isOnline -> {
                    val profile = player.playerProfile
                    item.editItemMeta<SkullMeta> { playerProfile = profile }
                    profileCache[player.uniqueId] = profile
                }
                player.uniqueId in profileCache -> item.editItemMeta<SkullMeta> { playerProfile = profileCache[player.uniqueId] }
                else -> player.playerProfile.update().thenAcceptAsync(
                    { profile ->
                        profileCache[player.uniqueId] = profile
                        item.editItemMeta<SkullMeta> { playerProfile = profile }
                    }, Bukkit.getScheduler().getMainThreadExecutor(abyss.plugin)
                )
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
