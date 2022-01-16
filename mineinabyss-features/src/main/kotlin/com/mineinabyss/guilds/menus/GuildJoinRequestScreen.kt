package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guilds.database.GuildJoinType
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.mineinabyss.extensions.addMemberToGuild
import com.mineinabyss.mineinabyss.extensions.getGuildJoinType
import com.mineinabyss.mineinabyss.extensions.getGuildMemberCount
import com.mineinabyss.mineinabyss.extensions.removeGuildQueueEntries
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer

@Composable
fun GuildUIScope.GuildJoinRequestScreen(from: OfflinePlayer) {
    PlayerLabel(Modifier.at(4, 0), from)
    AcceptGuildRequest(Modifier.at(1, 2), from)
    DeclineGuildRequest(Modifier.at(5, 2), from)
    BackButton(Modifier.at(4, 4))
}

@Composable
fun GuildUIScope.PlayerLabel(modifier: Modifier, newMember: OfflinePlayer) = Button(
    TitleItem.of("${ChatColor.YELLOW}${ChatColor.ITALIC}${newMember.name}"),
    modifier.size(2, 2)
)

@Composable
fun GuildUIScope.AcceptGuildRequest(modifier: Modifier, newMember: OfflinePlayer) = Button(
    TitleItem.of("${ChatColor.GREEN}Accept Join-Request"),
    modifier.size(3, 2).clickable {
        if (player.getGuildJoinType() == GuildJoinType.Request) {
            player.error("Your guild is in 'Invite-only' mode.")
            player.error("Change it to 'Any' or 'Request-only' mode to accept requests.")
            return@clickable
        }
        player.addMemberToGuild(newMember)
        if (player.getGuildMemberCount() < guildLevel * 5 + 1) {
            newMember.removeGuildQueueEntries(GuildJoinType.Request)
        }
        nav.back()
    }
)

@Composable
fun GuildUIScope.DeclineGuildRequest(modifier: Modifier, newMember: OfflinePlayer) = Button(
    TitleItem.of("${ChatColor.RED}Decline Join-Request"),
    modifier.size(3, 2).clickable {
        newMember.removeGuildQueueEntries(GuildJoinType.Request)
        player.sendMessage("${ChatColor.YELLOW}${ChatColor.BOLD}❌ ${ChatColor.YELLOW}You denied the join-request from ${newMember.name}")
        nav.back()
    }
)

@Composable
fun GuildUIScope.DeclineAllGuildRequests(modifier: Modifier) = Button(
    TitleItem.of("${ChatColor.RED}Decline All Join-Request"),
    modifier.clickable {
        player.removeGuildQueueEntries(GuildJoinType.Request, true)
        player.sendMessage("${ChatColor.YELLOW}${ChatColor.BOLD}❌ ${ChatColor.YELLOW}You denied all join-requests for your guild!")
        nav.back()
    }
)
