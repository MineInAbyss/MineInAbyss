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
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.mineinabyss.data.GuildRanks
import com.mineinabyss.mineinabyss.extensions.*
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun GuiyOwner.GuildMainMenu(player: Player) {
    Chest(listOf(player), "${Space.of(-18)}${ChatColor.WHITE}:main_guild_menu:",
        4, onClose = { exit() }) {
        if (player.getGuildRank() == GuildRanks.Owner) {
            CurrentGuildButton(player, Modifier.at(1, 1).clickable { guiy { CurrentGuildMenu(player) } })
            GuildOwnerButton(player, Modifier.at(4, 1))
            CreateGuildButton(player, Modifier.at(7, 1))
            GuildInvitesButton(player, Modifier.at(8, 0))
            LookForGuildButton(player, Modifier.at(8, 1))
        } else if (player.hasGuild() && player.getGuildRank() != GuildRanks.Owner) {
            CurrentGuildButton(player, Modifier.at(3, 1).clickable { guiy { CurrentGuildMenu(player) } })
            CreateGuildButton(player, Modifier.at(6, 1))
            LookForGuildButton(player, Modifier.at(7, 1))
            GuildInvitesButton(player, Modifier.at(8, 0))
            //LeaveGuildButton(player, Modifier.at(8,3))
        } else {
            CurrentGuildButton(player, Modifier.at(3, 1))
            CreateGuildButton(player, Modifier.at(6, 1))
            LookForGuildButton(player, Modifier.at(7, 1))
            GuildInvitesButton(player, Modifier.at(8, 0))
        }
    }
}

@Composable
fun CurrentGuildButton(player: Player, modifier: Modifier) {

    Grid(2, 2, modifier) {
        repeat(4) {
            if (player.hasGuild()) {
                Item(ItemStack(Material.PAPER).editItemMeta {
                    setDisplayName(
                        "${ChatColor.GOLD}${ChatColor.BOLD}Current Guild Info:"
                    )
                    lore = listOf(
                        "${ChatColor.YELLOW}${ChatColor.BOLD}Guild Name: ${ChatColor.YELLOW}${ChatColor.ITALIC}${player.getGuildName()}",
                        "${ChatColor.YELLOW}${ChatColor.BOLD}Guild Owner: ${ChatColor.YELLOW}${ChatColor.ITALIC}${
                            player.getGuildOwner().toPlayer()?.name
                        }",
                        "${ChatColor.YELLOW}${ChatColor.BOLD}Guild Level: ${ChatColor.YELLOW}${ChatColor.ITALIC}${player.getGuildLevel()}",
                        "${ChatColor.YELLOW}${ChatColor.BOLD}Guild Members: ${ChatColor.YELLOW}${ChatColor.ITALIC}${player.getGuildMemberCount()}"
                    )
                })
            } else {
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

    Grid(1, 1, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        if (player.hasGuild()) guiy { GuildMainMenu(player) }
        else player.nameGuild()
    })
    {
        repeat(1) {
            if (player.hasGuild()) {
                Item(ItemStack(Material.PAPER).editItemMeta {
                    /* Grayed out icon via custom model data */
                    setDisplayName("${ChatColor.GOLD}${ChatColor.ITALIC}${ChatColor.STRIKETHROUGH}Create a Guild")
                    lore = mutableListOf(
                        "${ChatColor.RED}You have to leave your current",
                        "${ChatColor.RED}Guild before you can create one."
                    )
                })
            } else {
                Item(ItemStack(Material.PAPER).editItemMeta {
                    //setCustomModelData(1) //Invis paper
                    setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}Create a Guild")

                })
            }
        }
    }
}

@Composable
fun LookForGuildButton(player: Player, modifier: Modifier) {
    val guildName = ItemStack(Material.PAPER).editItemMeta {
        setDisplayName("Guildname")
    }

    Grid(1, 1, modifier.clickable {

        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        if (player.hasGuild()) guiy { GuildMainMenu(player) }
        else {
            AnvilGUI.Builder()
                .title(":guild_request:")
                .itemLeft(guildName)
                //.preventClose()
                .plugin(guiyPlugin)
                .onClose { guiy { GuildMainMenu(player) } }
                .onComplete { player, guildName: String ->
                    player.lookForGuild(guildName)
                    AnvilGUI.Response.close()
                }
                .open(player)
        }
    })
    {
        repeat(1) {
            if (player.hasGuild()) {
                Item(ItemStack(Material.PAPER).editItemMeta {
                    /* Grayed out icon via custom model data */
                    setDisplayName("${ChatColor.GOLD}${ChatColor.ITALIC}${ChatColor.STRIKETHROUGH}Look for a Guild")
                    lore = mutableListOf(
                        "${ChatColor.RED}You have to leave your current",
                        "${ChatColor.RED}Guild before you can look for one."
                    )
                })
            } else {
                Item(ItemStack(Material.PAPER).editItemMeta {
                    //setCustomModelData(1) //Invis paper
                    setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}Look for a Guild")

                })
            }
        }
    }
}

@Composable
fun GuildInvitesButton(player: Player, modifier: Modifier) {
    val guildOwner = player.getGuildOwnerFromInvite().toPlayer()!!
    if (player.hasGuildInvite(guildOwner)) {
        Item(ItemStack(Material.PAPER).editItemMeta {
            setDisplayName("${ChatColor.DARK_GREEN}Manage Guild Invites")
            /* Icon that notifies player there are new invites */
        }, modifier.clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            guiy { GuildInvitesMenu(player) }
        })
    } else {
        Item(ItemStack(Material.PAPER).editItemMeta {
            setDisplayName("${ChatColor.DARK_GREEN}${ChatColor.STRIKETHROUGH}Manage Guild Invites")
            /* Custom Icon for "darkerened" out icon indicating no invites */
        }, modifier.clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        })
    }
}


@Composable
fun PreviousMenuButton(player: Player, modifier: Modifier) {
    Item(ItemStack(Material.PAPER).editItemMeta {
        setDisplayName("${ChatColor.DARK_AQUA}${ChatColor.ITALIC}Previous Menu")
    }, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        guiy { GuildMainMenu(player) }
    })
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
