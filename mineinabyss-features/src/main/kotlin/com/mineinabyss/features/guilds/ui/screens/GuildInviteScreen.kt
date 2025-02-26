package com.mineinabyss.features.guilds.ui.screens

import androidx.compose.runtime.Composable
import com.mineinabyss.features.guilds.ui.BackButton
import com.mineinabyss.features.guilds.ui.GuildUiState
import com.mineinabyss.features.guilds.ui.GuildViewModel
import com.mineinabyss.features.guilds.ui.components.GuildLabel
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.components.button.Button
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

