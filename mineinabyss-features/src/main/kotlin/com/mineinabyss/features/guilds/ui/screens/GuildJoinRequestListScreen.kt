package com.mineinabyss.features.guilds.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.mineinabyss.features.guilds.extensions.toOfflinePlayer
import com.mineinabyss.features.guilds.ui.BackButton
import com.mineinabyss.features.guilds.ui.GuildViewModel
import com.mineinabyss.features.guilds.ui.state.JoinRequest
import com.mineinabyss.guiy.components.HorizontalGrid
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.PlayerHead
import com.mineinabyss.guiy.components.items.PlayerHeadType
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.navigation.LocalBackGestureDispatcher
import com.mineinabyss.guiy.viewmodel.viewModel
import com.mineinabyss.idofront.textcomponents.miniMsg

@Composable
fun GuildJoinRequestListScreen(
    viewModel: GuildViewModel = viewModel(),
    navigateToJoinRequest: (JoinRequest) -> Unit,
) {
    val requests = viewModel.joinRequests.collectAsState().value
    val gestures = LocalBackGestureDispatcher.current
    GuildJoinRequestButton(requests, navigateToJoinRequest, Modifier.at(1, 1))
    Button(
        modifier = Modifier.at(8, 4),
        onClick = {
            viewModel.clearGuildJoinRequests()
            gestures?.onBack()
        }
    ) {
        Text("<red>Decline All Requests".miniMsg())
    }

    BackButton(Modifier.at(2, 4))
}

@Composable
fun GuildJoinRequestButton(
    joinRequests: List<JoinRequest>,
    navigateToJoinRequest: (JoinRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalGrid(modifier.size(9, 4)) {
        joinRequests.forEach { newMember ->
            Button(onClick = { navigateToJoinRequest(newMember) }) {
                val player = newMember.requester.toOfflinePlayer()
                PlayerHead(
                    player,
                    "<yellow><i>${player.name}",
                    "<blue>Click this to accept or deny the join-request.",
                    type = PlayerHeadType.FLAT
                )
            }
        }
    }
}
