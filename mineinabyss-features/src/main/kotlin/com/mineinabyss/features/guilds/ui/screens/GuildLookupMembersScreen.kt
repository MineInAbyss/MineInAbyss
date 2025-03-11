package com.mineinabyss.features.guilds.ui.screens

import androidx.compose.runtime.*
import com.mineinabyss.features.guilds.data.tables.GuildJoinType
import com.mineinabyss.features.guilds.data.tables.GuildRank
import com.mineinabyss.features.guilds.extensions.toOfflinePlayer
import com.mineinabyss.features.guilds.ui.BackButton
import com.mineinabyss.features.guilds.ui.state.GuildMemberUiState
import com.mineinabyss.features.guilds.ui.state.GuildUiState
import com.mineinabyss.features.guilds.ui.GuildViewModel
import com.mineinabyss.guiy.components.VerticalGrid
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.components.canvases.MAX_CHEST_HEIGHT
import com.mineinabyss.guiy.components.items.PlayerHead
import com.mineinabyss.guiy.components.items.PlayerHeadType
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.components.lists.NavbarPosition
import com.mineinabyss.guiy.components.lists.ScrollDirection
import com.mineinabyss.guiy.components.lists.Scrollable
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.viewmodel.viewModel

@Composable
fun GuildLookupMembersScreen(
    viewingGuild: GuildUiState,
    guildViewModel: GuildViewModel = viewModel(),
) {
    val height = viewingGuild.level.coerceIn(3, MAX_CHEST_HEIGHT)
    Chest(":space_-8::guild_lookup_members$height:", Modifier.height(height)) {
//    val owner = guildName.getOwnerFromGuildName()
        val memberInfo by guildViewModel.memberInfo.collectAsState()
        val guildLevel = viewingGuild.level
        val height = minOf(guildLevel.plus(2), MAX_CHEST_HEIGHT - 1)
        var line by remember { mutableStateOf(0) }
        val guildMembers = remember(viewingGuild.members) {
            //TODO is this actually sorting by the three items in compareby?
            viewingGuild.members
                .sortedWith(compareBy { it.uuid.toOfflinePlayer().isConnected; it.name; it.rank.ordinal })
                .filter { it.rank != GuildRank.OWNER }
        }

        val owner = viewingGuild.owner

        Scrollable(
            guildMembers, line,
            onLineChange = { line = it },
            ScrollDirection.VERTICAL,
            nextButton = { ScrollDownButton(Modifier.at(0, 4)) },
            previousButton = { ScrollUpButton(Modifier.at(0, 1)) },
            NavbarPosition.END, null
        ) { members ->
            VerticalGrid(Modifier.at(2, 1).size(5, minOf(guildLevel + 1, 4))) {
                members.forEach { member ->
                    Button {
                        GuildLabel(member)
                    }
                }
            }
        }

        GuildLabel(owner, Modifier.at(4, 0), headType = PlayerHeadType.LARGE_CENTER)
        RequestToJoinButton(
            viewingGuild,
            hasGuild = memberInfo != null,
            modifier = Modifier.at(4, height)
        )
        BackButton(Modifier.at(0, height))
    }
}

@Composable
fun GuildLabel(
    member: GuildMemberUiState,
    modifier: Modifier = Modifier,
    headType: PlayerHeadType = PlayerHeadType.FLAT,
) {
    PlayerHead(
        member.uuid.toOfflinePlayer(),
        "<gold><i>${member.name}",
        "<yellow><b>Guild Rank: <yellow><i>${member.rank}",
        type = headType,
        modifier = modifier
    )
}

@Composable
fun RequestToJoinButton(
    guild: GuildUiState,
    hasGuild: Boolean,
    guildViewModel: GuildViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val inviteOnly = guild.joinType == GuildJoinType.INVITE

    Button(modifier = modifier, enabled = !inviteOnly && !hasGuild, onClick = {
        guildViewModel.requestJoin(guild)
    }) {
        when {
            !inviteOnly && !hasGuild -> Text(
                "<green>REQUEST to join <dark_green><i>${guild.name}"
            )

            inviteOnly -> Text(
                "<red><st>REQUEST to join <i>${guild.name}",
                "<dark_red><i>This guild can currently only",
                "<dark_red><i>be joined via an invite."
            )

            else -> Text(
                "<red><st>REQUEST to join <i>${guild.name}",
                "<dark_red><i>You have to leave your Guild",
                "<dark_red><i>before requesting to join another."
            )
        }
    }
}
