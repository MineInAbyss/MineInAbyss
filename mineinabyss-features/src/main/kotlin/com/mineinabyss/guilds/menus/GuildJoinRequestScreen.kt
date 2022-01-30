package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.mineinabyss.data.GuildJoinType
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
fun GuildUIScope.PlayerLabel(modifier: Modifier, newMember: OfflinePlayer) = Button(modifier = modifier) {
    Text("${ChatColor.YELLOW}${ChatColor.ITALIC}${newMember.name}", modifier = Modifier.size(2, 2))
}

@Composable
fun GuildUIScope.AcceptGuildRequest(modifier: Modifier, newMember: OfflinePlayer) = Button(
    onClick = {
        if (player.getGuildJoinType() == GuildJoinType.Request) {
            player.error("Your guild is in 'Invite-only' mode.")
            player.error("Change it to 'Any' or 'Request-only' mode to accept requests.")
            return@Button
        }
        player.addMemberToGuild(newMember)
        if (player.getGuildMemberCount() < guildLevel * 5 + 1) {
            newMember.removeGuildQueueEntries(GuildJoinType.Request)
        }
        nav.back()
    },
    modifier = modifier
) {
    Text("${ChatColor.GREEN}Accept Join-Request", modifier = Modifier.size(3, 2))
}

@Composable
fun GuildUIScope.DeclineGuildRequest(modifier: Modifier, newMember: OfflinePlayer) = Button(
    modifier = modifier,
    onClick = {
        newMember.removeGuildQueueEntries(GuildJoinType.Request)
        player.sendMessage("${ChatColor.YELLOW}${ChatColor.BOLD}❌ ${ChatColor.YELLOW}You denied the join-request from ${newMember.name}")
        nav.back()
    }
) {
    Text("${ChatColor.RED}Decline Join-Request", modifier = Modifier.size(3, 2))
}

@Composable
fun GuildUIScope.DeclineAllGuildRequests(modifier: Modifier) = Button(
    modifier = modifier,
    onClick = {
        player.removeGuildQueueEntries(GuildJoinType.Request, true)
        player.sendMessage("${ChatColor.YELLOW}${ChatColor.BOLD}❌ ${ChatColor.YELLOW}You denied all join-requests for your guild!")
        nav.back()
    }
) {
    Text("${ChatColor.RED}Decline All Join-Request")
}