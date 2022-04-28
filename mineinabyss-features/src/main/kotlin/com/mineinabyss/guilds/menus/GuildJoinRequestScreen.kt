package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guilds.database.GuildJoinType
import com.mineinabyss.guilds.extensions.*
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.MessageQueue
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.messaging.error
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.jetbrains.exposed.sql.insert

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
        if (player.getGuildJoinType() == GuildJoinType.Invite) {
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
        val requestDeniedMessage =
            "${ChatColor.RED}Your request to join ${ChatColor.ITALIC}${player.getGuildName()} has been denied!"
        if (newMember.isOnline) newMember.player?.error(requestDeniedMessage)
        else {
            MessageQueue.insert {
                it[content] = requestDeniedMessage
                it[playerUUID] = newMember.uniqueId
            }
        }
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
