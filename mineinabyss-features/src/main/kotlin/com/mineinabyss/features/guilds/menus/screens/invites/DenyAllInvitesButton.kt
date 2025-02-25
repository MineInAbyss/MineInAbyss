package com.mineinabyss.features.guilds.menus.screens.invites

import androidx.compose.runtime.Composable
import com.mineinabyss.features.guilds.database.GuildJoinType
import com.mineinabyss.features.guilds.extensions.removeGuildQueueEntries
import com.mineinabyss.features.guilds.menus.GuildScreen
import com.mineinabyss.features.guilds.menus.GuildViewModel
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.inventory.CurrentPlayer
import com.mineinabyss.guiy.inventory.viewModel
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.entity.Player

@Composable
fun DenyAllInvitesButton(
    modifier: Modifier,
    guild: GuildViewModel = viewModel(),
    player: Player = CurrentPlayer,
) = Button(
    onClick = {
        player.removeGuildQueueEntries(GuildJoinType.INVITE, true)
        guild.nav.open(GuildScreen.MemberList(guild.guildLevel, player))
        player.info("<gold><b>❌<yellow>You denied all invites!")
    },
    modifier = modifier
) {
    Text("<red>Decline All Invites".miniMsg())
}
