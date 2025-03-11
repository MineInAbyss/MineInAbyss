package com.mineinabyss.features.guilds.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.mineinabyss.features.guilds.extensions.toOfflinePlayer
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.PlayerHead
import com.mineinabyss.guiy.components.items.PlayerHeadType
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.viewmodel.viewModel
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.OfflinePlayer

@Composable
fun GuildJoinRequestScreen(
    request: JoinRequest,
    navigateBack: () -> Unit,
    guildViewModel: GuildViewModel = viewModel(),
) {
    val requestPlayer = request.requester.toOfflinePlayer()
    val guild = guildViewModel.currentGuild.collectAsState().value ?: return
    Button(
        enabled = guild.canAcceptNewMembers,
        onClick = { guildViewModel.acceptJoinRequest(request); navigateBack() },
        modifier = Modifier.at(1, 1)
    ) {
        Text("<green>Accept GuildJoin-REQUEST".miniMsg(), modifier = Modifier.size(3, 3))
    }
    Button(
        onClick = { guildViewModel.declineJoinRequest(request); navigateBack() },
        modifier = Modifier.at(5, 1)
    ) {
        Text("<red>Decline GuildJoin-REQUEST".miniMsg(), modifier = Modifier.size(3, 3))
    }
    PlayerLabel(Modifier.at(4, 0), requestPlayer)
    BackButton(Modifier.at(4, 4))
}

@Composable
fun PlayerLabel(modifier: Modifier, newMember: OfflinePlayer) = Button(modifier = modifier) {
    PlayerHead(newMember, "<yellow><i>${newMember.name}", type = PlayerHeadType.LARGE_CENTER)
}
