package com.mineinabyss.features.guilds.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.mineinabyss.features.guilds.extensions.hasGuild
import com.mineinabyss.features.guilds.extensions.isGuildOwner
import com.mineinabyss.features.guilds.ui.CloseButton
import com.mineinabyss.features.guilds.ui.CreateGuildButton
import com.mineinabyss.features.guilds.ui.CurrentGuildButton
import com.mineinabyss.features.guilds.ui.GuildInvitesButton
import com.mineinabyss.features.guilds.ui.GuildLookupListButton
import com.mineinabyss.features.guilds.ui.GuildScreen
import com.mineinabyss.features.guilds.ui.GuildViewModel
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.inventory.CurrentPlayer
import com.mineinabyss.guiy.inventory.viewModel
import com.mineinabyss.guiy.layout.Column
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import org.bukkit.entity.Player

@Composable
fun HomeScreen(
    guild: GuildViewModel = viewModel(),
    player: Player = CurrentPlayer,
) {
    val guildOwner by remember { mutableStateOf(player.isGuildOwner()) }
    Row(Modifier.Companion.at(2, 1)) {
        if (player.hasGuild()) CurrentGuildButton(onClick = { guild.nav.open(GuildScreen.GuildInfo(guildOwner)) })
        else CreateGuildButton()

        Spacer(1)
        GuildLookupListButton()
    }

    Column(Modifier.Companion.at(8, 0)) {
        GuildInvitesButton()
    }

    CloseButton(Modifier.Companion.at(0, 3))
}
