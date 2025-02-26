package com.mineinabyss.features.guilds.ui.screens.invites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mineinabyss.features.guilds.ui.GuildScreen
import com.mineinabyss.features.guilds.ui.GuildViewModel
import com.mineinabyss.guiy.components.HorizontalGrid
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.PlayerHead
import com.mineinabyss.guiy.components.items.PlayerHeadType
import com.mineinabyss.guiy.inventory.CurrentPlayer
import com.mineinabyss.guiy.inventory.viewModel
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.size
import org.bukkit.entity.Player

@Composable
fun GuildInvites(
    modifier: Modifier = Modifier,
    player: Player = CurrentPlayer,
    guildViewModel: GuildViewModel = viewModel(),
) {
    val invites by guildViewModel.invites.collectAsState()
    HorizontalGrid(modifier.size(9, 4)) {
        invites.sortedBy { it.guild.memberCount }.forEach { invite ->
            Button(onClick = { guildViewModel.nav.open(GuildScreen.Invite(invite.guild)) }) {
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
