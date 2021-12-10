package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.font.NegativeSpace
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.mineinabyss.extensions.getGuildName
import com.mineinabyss.mineinabyss.extensions.getGuildOwner
import com.mineinabyss.mineinabyss.extensions.leaveGuild
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun GuiyOwner.CurrentGuildMenu(member: Player) {
    Chest(
        listOf(member), "${NegativeSpace.of(18)}${ChatColor.WHITE}:current_guild_menu:",
        6, onClose = { exit() }) {
        GuildOwnerLabel(member, Modifier.at(1, 1))
        GuildNameLabel(member, Modifier.at(3, 2))
        LeaveGuildButton(member, Modifier.at(8, 5))
    }
}

@Composable
fun GuildOwnerLabel(player: Player, modifier: Modifier) {
    repeat(1) {
        Item(ItemStack(Material.PAPER).editItemMeta {
            setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}Guild Owner: " +
                    "${ChatColor.YELLOW}${ChatColor.ITALIC}${player.getGuildOwner().toPlayer()}")
        })
    }
}

@Composable
fun GuildNameLabel(player: Player, modifier: Modifier) {
    repeat(1) {
        Item(ItemStack(Material.PAPER).editItemMeta {
            setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}Guild Name: " +
                    "${ChatColor.YELLOW}${ChatColor.ITALIC}${player.getGuildName()}")
        })
    }
}

@Composable
fun LeaveGuildButton(player: Player, modifier: Modifier) {
    Grid(1, 1, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        player.leaveGuild()
        player.closeInventory()
    })
    {
        repeat(1) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.RED}${ChatColor.ITALIC}Leave Guild")
            })
        }
    }
}


