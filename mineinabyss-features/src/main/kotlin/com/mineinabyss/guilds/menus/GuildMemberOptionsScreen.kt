package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.guilds.extensions.kickPlayerFromGuild
import com.mineinabyss.guilds.extensions.promotePlayerInGuild
import org.bukkit.ChatColor.*
import org.bukkit.OfflinePlayer

@Composable
fun GuildUIScope.GuildMemberOptionsScreen(member: OfflinePlayer) {
    /* Large playerhead or playermodel :pogo: */
    //TODO I'd like a row of buttons here that let you click on the exact rank to give a player,
    // with the final button being kick.
    PromoteGuildMember(member, Modifier.at(1, 1))
    KickGuildMember(member, Modifier.at(5, 1))
    BackButton(Modifier.at(2, 4))
}

@Composable
fun GuildUIScope.PromoteGuildMember(member: OfflinePlayer, modifier: Modifier) = Button(
    modifier = modifier,
    onClick = {
        player.promotePlayerInGuild(member)
        nav.back()
    }
) {
    Text("$BLUE${ITALIC}Promote Member", modifier = Modifier.size(3, 2))
}

@Composable
fun GuildUIScope.KickGuildMember(member: OfflinePlayer, modifier: Modifier) = Button(
    modifier = modifier,
    onClick = {
        player.kickPlayerFromGuild(member)
        nav.back()
    }
) {
    Text("$RED${ITALIC}Kick Member", modifier = Modifier.size(3, 2))
}


