package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.mineinabyss.extensions.kickPlayerFromGuild
import com.mineinabyss.mineinabyss.extensions.promotePlayerInGuild
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
    TitleItem.of("$BLUE${ITALIC}Promote Member"),
    modifier.size(3, 2).clickable {
        player.promotePlayerInGuild(member)
        nav.back()
    }
)

@Composable
fun GuildUIScope.KickGuildMember(member: OfflinePlayer, modifier: Modifier) = Button(
    TitleItem.of("$RED${ITALIC}Kick Member"),
    modifier.size(3, 2).clickable {
        player.kickPlayerFromGuild(member)
        nav.back()
    }
)


