package com.mineinabyss.features.guilds.ui.screens

import androidx.compose.runtime.*
import com.mineinabyss.features.guilds.data.tables.GuildJoinType
import com.mineinabyss.features.guilds.extensions.toOfflinePlayer
import com.mineinabyss.features.guilds.ui.BackButton
import com.mineinabyss.features.guilds.ui.GuildScreen
import com.mineinabyss.features.guilds.ui.GuildUiState
import com.mineinabyss.features.guilds.ui.GuildViewModel
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.guiy.components.VerticalGrid
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.canvases.MAX_CHEST_HEIGHT
import com.mineinabyss.guiy.components.items.PlayerHead
import com.mineinabyss.guiy.components.items.PlayerHeadType
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.components.lists.NavbarPosition
import com.mineinabyss.guiy.components.lists.ScrollDirection
import com.mineinabyss.guiy.components.lists.Scrollable
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.inventory.CurrentPlayer
import com.mineinabyss.guiy.inventory.viewModel
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.click.clickable
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.navigation.UniversalScreens
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.entity.Player

@Composable
fun GuildMemberListScreen(
    viewModel: GuildViewModel = viewModel(),
) {
    var line by remember { mutableStateOf(0) }
    val guild = viewModel.currentGuild.collectAsState().value ?: return
    val isCaptain = viewModel.isCaptainOrAbove.collectAsState().value
    val player = CurrentPlayer

    Scrollable(
        guild.members, line, ScrollDirection.VERTICAL,
        nextButton = { ScrollDownButton(Modifier.at(0, 3).clickable { line++ }) },
        previousButton = { ScrollUpButton(Modifier.at(0, 1).clickable { line-- }) },
        NavbarPosition.START, null
    ) { members ->
        VerticalGrid(Modifier.at(1, 1).size(5, minOf(guild.level + 1, 4))) {
            members.forEach { (name, member, rank) ->
                Button(onClick = {
                    if (member != player && isCaptain)
                        viewModel.nav.open(GuildScreen.MemberOptions(member.toOfflinePlayer()))
                }) {
                    PlayerHead(
                        member.toOfflinePlayer(), "<gold><i>${name}",
                        "<yellow><b>Guild Rank: <yellow><i>$rank",
                        type = PlayerHeadType.FLAT
                    )
                }
            }
        }
    }

    InviteToGuildButton(Modifier.at(7, 0), guild, isCaptain)

    val requests = viewModel.joinRequests.collectAsState().value
    val requestAmount = requests.size
    val plural = requestAmount != 1
    Button(
        enabled = requests.isNotEmpty() && isCaptain,
        /* Icon that notifies player there are new invites */
        modifier = Modifier.at(8, 0),
        onClick = { viewModel.nav.open(GuildScreen.JoinRequestList) }
    ) { enabled ->
        if (enabled) Text(
            "<dark_green><b>Manage Guild GuildJoin Requests",
            "<yellow><i>There ${if (plural) "are" else "is"} currently <gold><b>$requestAmount ",
            "<yellow><i>join-request${if (plural) "s" else ""} for your guild."
        )
        else Text(
            "<dark_green><b><st>Manage Guild GuildJoin Requests",
            "<red><i>There are currently no ",
            "<red><i>join-requests for your guild."
        )
    }

    Button(
        enabled = isCaptain,
        modifier = Modifier.at(0, 0),
        onClick = {
            viewModel.updateJoinType()
            //TODO manage title better
//            player.openInventory.title(":space_-8:${DecideMenus.decideMemberMenu(player, guild.joinType)}")
        }
    ) {
        Text(
            "<dark_green><b>Toggle Guild GuildJoin Type",
            "<yellow>Currently players can join via:<gold><i> ${guild.joinType.name}"
        )
    }

    BackButton(Modifier.at(0, minOf(guild.level + 1, MAX_CHEST_HEIGHT - 1)))
}

@Composable
fun ScrollDownButton(modifier: Modifier = Modifier) {
    Text("<green><b>Scroll Down", modifier = modifier)
}

@Composable
fun ScrollUpButton(modifier: Modifier = Modifier) {
    Text("<blue><b>Scroll Up", modifier = modifier)
}

@Composable
fun InviteToGuildButton(
    modifier: Modifier,
    guild: GuildUiState,
    isCaptain: Boolean,
    player: Player = CurrentPlayer,
    viewModel: GuildViewModel = viewModel(),
) {
    Button(
        enabled = isCaptain,
        modifier = modifier,
        onClick = {
            if (guild.joinType == GuildJoinType.REQUEST) {
                player.error("Your guild is in 'REQUEST-only' mode.")
                player.error("Change it to 'ANY' or 'INVITE-only' mode to invite others.")
                return@Button
            }
            viewModel.nav.open(
                UniversalScreens.Anvil(
                    AnvilGUI.Builder()
                        .title(":space_-61::guild_search_menu:")
                        .itemLeft(TitleItem.of("Player Name").editItemMeta { isHideTooltip = true })
                        .itemOutput(TitleItem.transparentItem)
                        .plugin(guiyPlugin)
                        .onClose { viewModel.nav.back() }
                        .onClick { _, snapshot ->
                            viewModel.invitePlayer(snapshot.text)
                            listOf(AnvilGUI.ResponseAction.close())
                        }
                ))
        }
    ) {
        Text("<yellow><b>INVITE Player to Guild")
    }
}
