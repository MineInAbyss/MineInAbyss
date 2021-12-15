package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.mineinabyss.extensions.changeStoredGuildName
import com.mineinabyss.mineinabyss.extensions.getGuildName
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun GuiyOwner.GuildOwnerMenu(player: Player) {
    Chest(
        listOf(player), "${Space.of(-18)}${ChatColor.WHITE}:guild_owner_menu:",
        6, onClose = { exit() }) {
        GuildMemberManagement(player, Modifier.at(2, 0))
        GuildRenameButton(player, Modifier.at(5, 0))
        GuildHouseButton(player, Modifier.at(2, 3))
        GuildRelationshipButton(player, Modifier.at(5, 3))
        GuildDisbandButton(player, Modifier.at(8, 5))
        PreviousMenuButton(player, Modifier.at(2, 5))
    }
}

@Composable
fun GuildMemberManagement(player: Player, modifier: Modifier) {
    Grid(2, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        guiy { GuildMemberManagementMenu(player) }
    })
    {
        repeat(4) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.GREEN}${ChatColor.BOLD}Guild Member List")
            })
        }
    }
}

@Composable
fun GuildRenameButton(player: Player, modifier: Modifier) {
    Grid(2, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        player.renameGuild()
    })
    {
        repeat(4) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.YELLOW}${ChatColor.BOLD}Change Guild Name")
            })
        }
    }
}

@Composable
fun GuildHouseButton(player: Player, modifier: Modifier) {
    Grid(2, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
    })
    {
        repeat(4) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}Change Guild House")
            })
        }
    }
}

@Composable
fun GuildRelationshipButton(player: Player, modifier: Modifier) {
    Grid(2, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        //guiy { GuildRelationshipMenu(player) }
    })
    {
        repeat(4) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.BLUE}${ChatColor.BOLD}Guild Relationships")
            })
        }
    }
}

@Composable
fun GuildDisbandButton(player: Player, modifier: Modifier) {
    Grid(1, 1, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        guiy { GuildDisbandMenu(player) }
    })
    {
        repeat(1) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.RED}${ChatColor.BOLD}Disband Guild")
            })
        }
    }
}

fun Player.renameGuild() {
    val guildRenamePaper = ItemStack(Material.PAPER).editItemMeta {
        setDisplayName(player?.getGuildName())
        setCustomModelData(1)
    }

    AnvilGUI.Builder()
        .title(":guild_naming:")
        .itemLeft(guildRenamePaper)
        .plugin(guiyPlugin)
        .onClose { guiy { GuildOwnerMenu(player!!) } }
        .onComplete { player, guildName: String ->
            player.changeStoredGuildName(guildName)
            AnvilGUI.Response.close()
        }
        .open(player)
}
