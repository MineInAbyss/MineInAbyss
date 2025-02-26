package com.mineinabyss.features.guilds.ui

import androidx.compose.runtime.Composable
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.PlayerHead
import com.mineinabyss.guiy.components.items.PlayerHeadType
import com.mineinabyss.guiy.inventory.viewModel
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size

@Composable
fun GuildInviteScreen(
    invitedTo: GuildUiState,
    guildViewModel: GuildViewModel = viewModel(),
) {
    GuildLabel(invitedTo, Modifier.at(4, 0))

    Button(
        modifier = Modifier.at(1, 1),
        onClick = { guildViewModel.acceptInvite(invitedTo.id) },
    ) {
        Text("<green>Accept INVITE", modifier = Modifier.size(3, 3))
    }

    Button(
        modifier = Modifier.at(5, 1),
        onClick = { guildViewModel.declineInvite(invitedTo.id) }
    ) {
        Text("<red>Decline INVITE", modifier = Modifier.size(3, 3))
    }

    BackButton(Modifier.at(4, 4))
}

@Composable
fun GuildLabel(guild: GuildUiState, modifier: Modifier) = Button {
    PlayerHead(
        guild.owner.uuid.toOfflinePlayer(),
        "<gold><b>Current Guild Info</b>",
        "<yellow><b>Guild Name:</b> <i>${guild.name}",
        "<yellow><b>Guild Owner:</b> <i>${guild.owner.name}",
        "<yellow><b>Guild Level:</b> <i>${guild.level}",
        "<yellow><b>Guild Members:</b> <i>${guild.memberCount}",
        type = PlayerHeadType.LARGE_CENTER,
        modifier = modifier
    )
}
