package com.mineinabyss.features.guilds.ui.screens.invites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.mineinabyss.features.guilds.data.tables.GuildJoinType
import com.mineinabyss.features.guilds.extensions.removeGuildQueueEntries
import com.mineinabyss.features.guilds.ui.GuildScreen
import com.mineinabyss.features.guilds.ui.GuildViewModel
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.inventory.CurrentPlayer
import com.mineinabyss.guiy.inventory.viewModel
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.entity.Player

@Composable
fun DenyAllInvitesButton(
    modifier: Modifier,
    guildViewModel: GuildViewModel = viewModel(),
    player: Player = CurrentPlayer,
) {
    val guild = guildViewModel.currentGuild.collectAsState().value
    if (guild == null) return

    Button(
        onClick = {
            player.removeGuildQueueEntries(GuildJoinType.INVITE, true)
            guildViewModel.nav.open(GuildScreen.MemberList(guild.level, player))
            player.info("<gold><b>❌<yellow>You denied all invites!")
        },
        modifier = modifier
    ) {
        Text("<red>Decline All Invites".miniMsg())
    }
}
