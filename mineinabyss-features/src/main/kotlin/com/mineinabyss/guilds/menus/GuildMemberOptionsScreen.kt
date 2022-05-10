package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guilds.database.GuildRanks
import com.mineinabyss.guilds.extensions.getGuildRank
import com.mineinabyss.guilds.extensions.kickPlayerFromGuild
import com.mineinabyss.guilds.extensions.promotePlayerInGuild
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.messaging.miniMsg
import org.bukkit.OfflinePlayer

@Composable
fun GuildUIScope.GuildMemberOptionsScreen(member: OfflinePlayer) {
    /* Large playerhead or playermodel :pogo: */
    //TODO I'd like a row of buttons here that let you click on the exact rank to give a player,
    // with the final button being kick.
    PromoteGuildMember(member, Modifier.at(1, 1))
    KickGuildMember(member, Modifier.at(5, 1))
    BackButton(Modifier.at(4, 4))
}

@Composable
fun GuildUIScope.PromoteGuildMember(member: OfflinePlayer, modifier: Modifier) = Button(
    modifier = modifier,
    enabled = (player.getGuildRank() == GuildRanks.Owner || player.getGuildRank() == GuildRanks.Captain),
    onClick = {
        player.promotePlayerInGuild(member)
        nav.back()
    }
) {
    Text("<blue><i>Promote Member".miniMsg(), modifier = Modifier.size(3, 3))
}

@Composable
fun GuildUIScope.KickGuildMember(member: OfflinePlayer, modifier: Modifier) = Button(
    modifier = modifier,
    onClick = {
        player.kickPlayerFromGuild(member)
        nav.back()
    }
) {
    Text("<red><i>Kick Member".miniMsg(), modifier = Modifier.size(3, 3))
}


