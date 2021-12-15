package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.mineinabyss.extensions.deleteGuild
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun GuiyOwner.GuildDisbandMenu(player: Player) {
    Chest(
        listOf(player), "${Space.of(-18)}${ChatColor.WHITE}:disband_guild_menu:",
        4, onClose = { exit() }) {
        ConfirmGuildDisbanding(player, Modifier.at(1, 1))
        CancelGuildDisbanding(player, Modifier.at(5, 1))
    }
}

@Composable
fun ConfirmGuildDisbanding(player: Player, modifier: Modifier) {
    Grid(3, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        player.deleteGuild()
        player.closeInventory()
    })
    {
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.GREEN}${ChatColor.BOLD}Confirm Guild Disbanding")
            })
        }
    }
}

@Composable
fun CancelGuildDisbanding(player: Player, modifier: Modifier) {
    Grid(3, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        guiy { GuildOwnerMenu(player) }
    })
    {
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.RED}${ChatColor.BOLD}Cancel Guild Disbanding")
            })
        }
    }
}
