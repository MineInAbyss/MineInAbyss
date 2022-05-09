package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guilds.database.GuildJoinType
import com.mineinabyss.guilds.extensions.*
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.MessageQueue
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.head
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import org.bukkit.OfflinePlayer
import org.jetbrains.exposed.sql.insert

@Composable
fun GuildUIScope.GuildJoinRequestScreen(from: OfflinePlayer) {
    PlayerLabel(Modifier.at(4, 0), from)
    AcceptGuildRequest(Modifier.at(1, 1), from)
    DeclineGuildRequest(Modifier.at(5, 1), from)
    BackButton(Modifier.at(4, 4))
}

@Composable
fun GuildUIScope.PlayerLabel(modifier: Modifier, newMember: OfflinePlayer) = Button(modifier = modifier) {
    Item(newMember.head("<yellow><i>${newMember.name}", isCenterOfInv = true, isLarge = true))
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
        newMember.removeGuildQueueEntries(GuildJoinType.Request)
        if (player.getGuildMemberCount() < guildLevel * 5 + 1) {
            newMember.removeGuildQueueEntries(GuildJoinType.Request)
        }
        nav.back()
    },
    modifier = modifier
) {
    Text("<green>Accept Join-Request", modifier = Modifier.size(3, 3))
}

@Composable
fun GuildUIScope.DeclineGuildRequest(modifier: Modifier, newMember: OfflinePlayer) = Button(
    modifier = modifier,
    onClick = {
        newMember.removeGuildQueueEntries(GuildJoinType.Request)
        player.info("<yellow><b>❌ <yellow>You denied the join-request from ${newMember.name}")
        val requestDeniedMessage =
            "<red>Your request to join <i>${player.getGuildName()} has been denied!"
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
    Text("<red>Decline Join-Request", modifier = Modifier.size(3, 3))
}

@Composable
fun GuildUIScope.DeclineAllGuildRequests(modifier: Modifier) = Button(
    modifier = modifier,
    onClick = {
        player.removeGuildQueueEntries(GuildJoinType.Request, true)
        player.info("<yellow><b>❌ <yellow>You denied all join-requests for your guild!")
        nav.back()
    }
) {
    Text("<red>Decline All Join-Request")
}
