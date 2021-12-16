package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.mineinabyss.data.GuildRanks
import com.mineinabyss.mineinabyss.extensions.*
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.ChatColor.*
import org.bukkit.Sound
import org.bukkit.entity.Player

@Composable
fun GuiyOwner.GuildMainMenu(player: Player) {
    Chest(listOf(player), "${Space.of(-18)}$WHITE:main_guild_menu:",
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

    Item(
        if (player.hasGuild()) {
            TitleItem.of(
                "$GOLD${BOLD}Current Guild Info:",
                "$YELLOW${BOLD}Guild Name: $YELLOW$ITALIC${player.getGuildName()}",
                "$YELLOW${BOLD}Guild Owner: $YELLOW$ITALIC${player.getGuildOwner().toPlayer()?.name}",
                "$YELLOW${BOLD}Guild Level: $YELLOW$ITALIC${player.getGuildLevel()}",
                "$YELLOW${BOLD}Guild Members: $YELLOW$ITALIC${player.getGuildMemberCount()}"
            )
        } else {
            TitleItem.of(
                "$GOLD$BOLD${STRIKETHROUGH}View Guild Information",
                "${RED}You are not a member of any guild."
            )
        },
        modifier.size(2, 2)
    )
}

@Composable
fun GuildOwnerButton(player: Player, modifier: Modifier) {
    Item(
        TitleItem.of("$RED${BOLD}View Owner Information"),
        modifier.size(2, 2).clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            guiy { GuildOwnerMenu(player) }
        }
    )
}

@Composable
fun CreateGuildButton(player: Player, modifier: Modifier) {
    Item(
        if (player.hasGuild()) {
            TitleItem.of(
                "$GOLD$ITALIC${STRIKETHROUGH}Create a Guild",
                "${RED}You have to leave your current",
                "${RED}Guild before you can create one."
            )
        } else {
            TitleItem.of("$GOLD${BOLD}Create a Guild")
        },
        modifier.clickable {
            if (!player.hasGuild()) {
                player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
                player.nameGuild()
            }
        }
    )
}

@Composable
fun LookForGuildButton(player: Player, modifier: Modifier) {
    Item(
        if (player.hasGuild()) {
            TitleItem.of(
                "$GOLD$ITALIC${STRIKETHROUGH}Look for a Guild",
                "${RED}You have to leave your current",
                "${RED}Guild before you can look for one."
            )
        } else {
            TitleItem.of("$GOLD${BOLD}Look for a Guild")
        },
        modifier.clickable {
            if (!player.hasGuild()) {
                player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
                AnvilGUI.Builder()
                    .title(":guild_request:")
                    .itemLeft(TitleItem.of("Guildname"))
                    //.preventClose()
                    .plugin(guiyPlugin)
                    .onClose { guiy { GuildMainMenu(player) } }
                    .onComplete { player, guildName: String ->
                        player.lookForGuild(guildName)
                        AnvilGUI.Response.close()
                    }
                    .open(player)
            }
        }
    )
}

@Composable
fun GuildInvitesButton(player: Player, modifier: Modifier) {
    val guildOwner = player.getGuildOwnerFromInvite().toPlayer()!!
    if (player.hasGuildInvite(guildOwner)) {
        /* Icon that notifies player there are new invites */
        Item(TitleItem.of("${DARK_GREEN}Manage Guild Invites"), modifier.clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            guiy { GuildInvitesMenu(player) }
        })
    } else {
        /* Custom Icon for "darkerened" out icon indicating no invites */
        Item(TitleItem.of("$DARK_GREEN${STRIKETHROUGH}Manage Guild Invites"), modifier.clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        })
    }
}


@Composable
fun PreviousMenuButton(player: Player, modifier: Modifier) {
    //TODO convert all these reused playSound calls into a Button compostable.
    Item(TitleItem.of("$DARK_AQUA${ITALIC}Previous Menu"), modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        guiy { GuildMainMenu(player) }
    })
}

fun Player.nameGuild() {
    val guildRenamePaper = TitleItem.of("Guildname")

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
