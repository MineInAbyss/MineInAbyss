package com.mineinabyss.features.guilds.ui.screens.invites

import androidx.compose.runtime.Composable
import com.mineinabyss.features.guilds.ui.BackButton
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at

@Composable
fun GuildInviteListScreen() {
    GuildInvites(Modifier.at(1, 1))
    DenyAllInvitesButton(Modifier.at(8, 4))
    BackButton(Modifier.at(2, 4))
}

