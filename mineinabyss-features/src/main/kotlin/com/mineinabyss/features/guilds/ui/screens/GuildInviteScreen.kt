package com.mineinabyss.features.guilds.ui.screens

import androidx.compose.runtime.Composable
import com.mineinabyss.features.guilds.ui.BackButton
import com.mineinabyss.features.guilds.ui.state.GuildUiState
import com.mineinabyss.features.guilds.ui.GuildViewModel
import com.mineinabyss.features.guilds.ui.components.GuildLabel
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.navigation.LocalBackGestureDispatcher
import com.mineinabyss.guiy.viewmodel.viewModel

@Composable
fun GuildInviteScreen(
    invitedTo: GuildUiState,
    guildViewModel: GuildViewModel = viewModel(),
) = Chest(":space_-8::guild_inbox_handle_menu:", Modifier.height(5)) {
    GuildLabel(invitedTo, Modifier.at(4, 0))
    val gestures = LocalBackGestureDispatcher.current
    Button(
        modifier = Modifier.at(1, 1),
        onClick = { guildViewModel.acceptInvite(invitedTo.id, onSuccess = { gestures?.onBack() }) },
    ) {
        Text("<green>Accept INVITE", modifier = Modifier.size(3, 3))
    }

    Button(
        modifier = Modifier.at(5, 1),
        onClick = {
            guildViewModel.declineInvite(invitedTo.id, onSuccess = { remaining ->
                gestures?.onBack()
//            if (remaining > 1)  nav.back()
//            else nav.reset()
            })
        }
    ) {
        Text("<red>Decline INVITE", modifier = Modifier.size(3, 3))
    }

    BackButton(Modifier.at(4, 4))
}

