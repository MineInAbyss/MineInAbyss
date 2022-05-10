package com.mineinabyss.guilds.menus

import androidx.compose.runtime.*
import com.mineinabyss.guilds.database.GuildJoinType
import com.mineinabyss.guilds.extensions.*
import com.mineinabyss.guilds.menus.JoinTypeIcon.any
import com.mineinabyss.guilds.menus.JoinTypeIcon.invite
import com.mineinabyss.guilds.menus.JoinTypeIcon.request
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.head
import com.mineinabyss.helpers.ui.UniversalScreens
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.miniMsg
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

@Composable
fun GuildUIScope.GuildMemberListScreen() {
    ManageGuildMembersButton(Modifier.at(1, 1))
    BackButton(Modifier.at(0, guildLevel + 1))

    InviteToGuildButton(Modifier.at(7, 0))
    ManageGuildJoinRequestsButton(Modifier.at(8, 0))
    ToggleGuildJoinTypeButton(Modifier.at(0, 0))
}

@Composable
fun GuildUIScope.ManageGuildMembersButton(modifier: Modifier) {
    Grid(modifier.size(5, guildLevel)) {
        player.getGuildMembers().sortedBy { it.first; it.second.name }.forEach { (rank, member) ->
            Button(onClick = {
                if (member != player && player.isAboveCaptain()) {
                    nav.open(GuildScreen.MemberOptions(member))
                }
            }) {
                Item(
                    member.head(
                        "<gold><i>${member.name}".miniMsg(),
                        "<yellow><b>Guild Rank: <yellow><i>$rank".miniMsg(),
                        isFlat = true
                    )
                )
            }
        }
    }
}

@Composable
fun GuildUIScope.InviteToGuildButton(modifier: Modifier) {
    val guildInvitePaper = TitleItem.of("Player Name")
    Button(
        enabled = player.isAboveCaptain(),
        modifier = modifier,
        onClick = {
            //if (player.isAboveCaptain()) return@Button
            if (player.getGuildJoinType() == GuildJoinType.Request) {
                player.error("Your guild is in 'Request-only' mode.")
                player.error("Change it to 'Any' or 'Invite-only' mode to invite others.")
                return@Button
            }
            nav.open(UniversalScreens.Anvil(
                AnvilGUI.Builder()
                    .title("${Space.of(-64)}${Space.of(1)}:guild_search_menu:")
                    .itemLeft(guildInvitePaper)
                    .plugin(guiyPlugin)
                    .onClose { nav.back() }
                    .onComplete { player, invitedPlayer: String ->
                        player.invitePlayerToGuild(invitedPlayer)
                        AnvilGUI.Response.close()
                    }
            ))
        }
    ) {
        Text("<yellow><b>Invite Player to Guild".miniMsg())
    }
}

@Composable
fun GuildUIScope.ManageGuildJoinRequestsButton(modifier: Modifier) {
    val requestAmount = player.getNumberOfGuildRequests()
    val plural = requestAmount != 1
    Button(
        enabled = player.hasGuildRequest(),
        /* Icon that notifies player there are new invites */
        modifier = modifier,
        onClick = {
            if (player.isAboveCaptain()) {
                nav.open(GuildScreen.JoinRequestList)
            }
        }
    ) { enabled ->
        if (enabled) Text(
            "<dark_green><b>Manage Guild Join Requests".miniMsg(),
            "<yellow><i>There ${if (plural) "are" else "is"} currently <gold><b>$requestAmount ".miniMsg(),
            "<yellow><i>join-request${if (plural) "s" else ""} for your guild.".miniMsg()
        )
        else Text(
            "<dark_green><b><st>Manage Guild Join Requests".miniMsg(),
            "<red><i>There are currently no ".miniMsg(),
            "<red><i>join-requests for your guild.".miniMsg()
        )
    }

}

@Composable
fun GuildUIScope.ToggleGuildJoinTypeButton(modifier: Modifier) {
    var joinType by remember { mutableStateOf(player.getGuildJoinType()) }
    val item = if (joinType == GuildJoinType.Any) any else if (joinType == GuildJoinType.Invite) invite else request
    Button(
        modifier = modifier,
        onClick = {
            if (player.isAboveCaptain()) {
                player.changeGuildJoinType()
                joinType = player.getGuildJoinType()
            }
        }
    ) {
        Item(item)
    }
}

object JoinTypeIcon {
    val any = ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(4)
        displayName("<dark_green><b>Toggle Guild Join Type".miniMsg())
        lore(listOf("<yellow>Currently players can join via:<gold><i> Any".miniMsg()))
    }

    val invite = ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(5)
        displayName("<dark_green><b>Toggle Guild Join Type".miniMsg())
        lore(listOf("<yellow>Currently players can join via:<gold><i> Invite".miniMsg()))
    }

    val request = ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(6)
        displayName("<dark_green><b>Toggle Guild Join Type".miniMsg())
        lore(listOf("<yellow>Currently players can join via:<gold><i> Request".miniMsg()))
    }
}
