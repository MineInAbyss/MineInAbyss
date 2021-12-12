package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.idofront.font.NegativeSpace
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.mineinabyss.extensions.createGuild
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun GuiyOwner.CreateGuildMenu(player: Player) {
    Chest(
        listOf(player), "${NegativeSpace.of(18)}${ChatColor.WHITE}:create_guild_menu:",
        4, onClose = { exit() }) {
        GuildSetNameButton(player, Modifier.at(4,2))
    }
}

/* Open anvil menu for text input ui*/
@Composable
fun GuildSetNameButton(player: Player, modifier: Modifier) {
    Grid(3, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        player.nameGuild()
    })
    {
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.YELLOW}${ChatColor.ITALIC}Set Guild name")
            })
        }
    }
}

fun Player.nameGuild() {
    val guildRenamePaper = ItemStack(Material.PAPER).editItemMeta {
        setDisplayName("Guildname")
        setCustomModelData(1)
    }

    AnvilGUI.Builder()
        .title(":guild_naming:")
        .itemLeft(guildRenamePaper)
        //.preventClose()
        .plugin(guiyPlugin)
        .onComplete { player, guildName: String ->
            player.createGuild(guildName)
            AnvilGUI.Response.close()
        }
        .open(player)
}
