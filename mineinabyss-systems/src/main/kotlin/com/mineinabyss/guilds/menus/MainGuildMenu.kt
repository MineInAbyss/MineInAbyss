package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.idofront.font.NegativeSpace
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.mineinabyss.data.GuildRanks
import com.mineinabyss.mineinabyss.extensions.guildRank
import com.mineinabyss.mineinabyss.extensions.hasGuild
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun GuiyOwner.GuildMainMenu(player: Player) {
    Chest(listOf(player), "${NegativeSpace.of(18)}${ChatColor.WHITE}:main_guild_menu:",
        4, onClose = { exit() }) {
        if (player.guildRank() == GuildRanks.Owner) {
        //if (player.hasGuild()) {
            CurrentGuildButton(player, Modifier.at(1, 1))
            GuildOwnerButton(player, Modifier.at(4,1))
            CreateGuildButton(player, Modifier.at(7, 1))

        }
        else {
            CurrentGuildButton(player, Modifier.at(3, 1))
            CreateGuildButton(player, Modifier.at(6, 1))
        }
    }
}

@Composable
fun CurrentGuildButton(player: Player, modifier: Modifier) {
    Grid(2, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)

        /* If player is not part of a guild, return to main menu */
        if (player.hasGuild()) guiy { CurrentGuildMenu(player) }
        else guiy { GuildMainMenu(player) }
    })
    {
        repeat(4) {
            if (player.hasGuild()){
                Item(ItemStack(Material.PAPER).editItemMeta {
                    setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}View Guild Information")
                })
            }
            else {
                Item(ItemStack(Material.PAPER).editItemMeta {
                    setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}${ChatColor.STRIKETHROUGH}View Guild Information")
                    lore = mutableListOf("${ChatColor.RED}You are not a member of any guild.")
                })
            }

        }
    }
}

@Composable
fun GuildOwnerButton(player: Player, modifier: Modifier) {
    Grid(2, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        guiy { GuildOwnerMenu(player) }
    })
    {
        repeat(4) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.RED}${ChatColor.BOLD}View Owner Information")
            })
        }
    }
}

@Composable
fun CreateGuildButton(player: Player, modifier: Modifier) {

    Grid(3, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        if (player.hasGuild()) guiy { GuildMainMenu(player) }
        else guiy { CreateGuildMenu(player) }
    })
    {
        repeat(1) {
            if (player.hasGuild()) {
                Item(ItemStack(Material.PAPER).editItemMeta {
                        /* Grayed out icon via custom model data */
                        setDisplayName("${ChatColor.GOLD}${ChatColor.ITALIC}${ChatColor.STRIKETHROUGH}Create a Guild")
                        lore = mutableListOf("${ChatColor.RED}You have to leave your current Guild before you can create one.")
                })
            }
            else {
                Item(ItemStack(Material.PAPER).editItemMeta {
                    //setCustomModelData(1) //Invis paper
                    setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}Create a Guild")

                })
            }
        }
    }
}


