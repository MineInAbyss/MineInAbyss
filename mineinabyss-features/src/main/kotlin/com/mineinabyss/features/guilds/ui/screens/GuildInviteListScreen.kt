package com.mineinabyss.features.guilds.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mineinabyss.features.guilds.data.tables.GuildJoinType
import com.mineinabyss.features.guilds.extensions.removeGuildQueueEntries
import com.mineinabyss.features.guilds.ui.BackButton
import com.mineinabyss.features.guilds.ui.GuildScreen
import com.mineinabyss.features.guilds.ui.GuildUiState
import com.mineinabyss.features.guilds.ui.GuildViewModel
import com.mineinabyss.guiy.components.HorizontalGrid
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.PlayerHead
import com.mineinabyss.guiy.components.items.PlayerHeadType
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.inventory.CurrentPlayer
import com.mineinabyss.guiy.viewmodel.viewModel
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.viewmodel.viewModel
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.entity.Player

@Composable
fun GuildInviteListScreen(
    guildViewModel: GuildViewModel = viewModel(),
    player: Player = CurrentPlayer,
    navigateToMembersList: () -> Unit = {},
) {
    val guild = guildViewModel.currentGuild.collectAsState().value ?: return
    GuildInvites(Modifier.at(1, 1))
    Button(
        onClick = {
            player.removeGuildQueueEntries(GuildJoinType.INVITE, true)
            navigateToMembersList()
            player.info("<gold><b>❌<yellow>You denied all invites!")
        },
        modifier = Modifier.at(8, 4)
    ) {
        Text("<red>Decline All Invites".miniMsg())
    }
    BackButton(Modifier.at(2, 4))
}


@Composable
fun GuildInvites(
    modifier: Modifier = Modifier,
    player: Player = CurrentPlayer,
    guildViewModel: GuildViewModel = viewModel(),
    navigateToInvites: () -> Unit = {}, //guildViewModel.nav.open(GuildScreen.InviteScreen(invite.guild))
) {
    val invites by guildViewModel.invites.collectAsState()
    HorizontalGrid(modifier.size(9, 4)) {
        invites.sortedBy { it.guild.memberCount }.forEach { invite ->
            Button(onClick = { navigateToInvites() }) {
                PlayerHead(
                    player,
                    "<gold><b>Guildname: <yellow><i>${invite.guild.name}",
                    "<blue>Click this to accept or deny invite.",
                    "<blue>Info about the guild can also be found in here.",
                    type = PlayerHeadType.FLAT,
                )
            }
        }
    }
}
