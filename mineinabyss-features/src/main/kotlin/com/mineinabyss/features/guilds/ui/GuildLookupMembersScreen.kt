package com.mineinabyss.features.guilds.ui

import androidx.compose.runtime.*
import com.mineinabyss.features.guilds.data.tables.GuildJoinType
import com.mineinabyss.features.guilds.data.tables.GuildRank
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.guiy.components.VerticalGrid
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.canvases.MAX_CHEST_HEIGHT
import com.mineinabyss.guiy.components.items.PlayerHead
import com.mineinabyss.guiy.components.items.PlayerHeadType
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.components.lists.NavbarPosition
import com.mineinabyss.guiy.components.lists.ScrollDirection
import com.mineinabyss.guiy.components.lists.Scrollable
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.click.clickable
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.OfflinePlayer

@Composable
fun GuildViewModel.GuildLookupMembersScreen(guildName: String) {
    val owner = guildName.getOwnerFromGuildName()
    val guildLevel = owner.getGuildLevel()
    val height = minOf(guildLevel.plus(2), MAX_CHEST_HEIGHT - 1)
    var line by remember { mutableStateOf(0) }
    val guildMembers = remember {
        guildName.getGuildMembers().sortedWith(compareBy { it.player.isConnected; it.player.name; it.rank.ordinal })
            .filter { it.rank != GuildRank.OWNER }
    }

    Scrollable(
        guildMembers, line, ScrollDirection.VERTICAL,
        nextButton = { ScrollDownButton(Modifier.at(0, 4).clickable { line++; this.clickType }) },
        previousButton = { ScrollUpButton(Modifier.at(0, 1).clickable { line-- }) },
        NavbarPosition.END, null
    ) { members ->
        VerticalGrid(Modifier.at(2, 1).size(5, minOf(guildLevel + 1, 4))) {
            members.forEach { (rank, member) ->
                Button {
                    PlayerHead(
                        owner,
                        "<gold><i>${member.name}",
                        "<yellow><b>Guild Rank: <yellow><i>$rank",
                        type = PlayerHeadType.FLAT
                    )
                }
            }
        }
    }

    GuildLabel(Modifier.at(4, 0), owner)
    BackButton(Modifier.at(0, height))
    RequestToJoinButton(Modifier.at(4, height), owner, guildName)
}

@Composable
fun GuildLabel(modifier: Modifier, owner: OfflinePlayer) {
    PlayerHead(
        owner,
        "<gold><i>${owner.name}",
        "<yellow><b>Guild Rank: <yellow><i>${owner.getGuildRank()}",
        type = PlayerHeadType.LARGE_CENTER,
        modifier = modifier
    )
}

@Composable
fun GuildViewModel.RequestToJoinButton(modifier: Modifier, owner: OfflinePlayer, guildName: String) {
    val inviteOnly = owner.getGuildJoinType() == GuildJoinType.INVITE
    Button(modifier = modifier, onClick = {
        if (!inviteOnly && !player.hasGuild())
            player.requestToJoin(guildName)
    }) {
        if (!inviteOnly && !player.hasGuild()) {
            Text("<green>REQUEST to join <dark_green><i>$guildName".miniMsg())
        } else if (inviteOnly) {
            Text(
                "<red><st>REQUEST to join <i>$guildName".miniMsg(),
                "<dark_red><i>This guild can currently only".miniMsg(),
                "<dark_red><i>be joined via an invite.".miniMsg()
            )
        } else if (player.hasGuild()) {
            Text(
                "<red><st>REQUEST to join <i>$guildName".miniMsg(),
                "<dark_red><i>You have to leave your Guild".miniMsg(),
                "<dark_red><i>before requesting to join another.".miniMsg()
            )
        }
    }
}
