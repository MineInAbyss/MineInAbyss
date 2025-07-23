package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.database.GuildJoinType
import com.mineinabyss.features.guilds.database.GuildMessageQueue
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.OfflinePlayer
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

@Composable
fun GuildUIScope.GuildJoinRequestScreen(from: OfflinePlayer) {
    PlayerLabel(Modifier.at(4, 0), from)
    AcceptGuildRequestButton(Modifier.at(1, 1), from)
    DeclineGuildRequestButton(Modifier.at(5, 1), from)
    BackButton(Modifier.at(4, 4))
}

@Composable
fun PlayerLabel(modifier: Modifier, newMember: OfflinePlayer) = Button(modifier = modifier) {
    Item(TitleItem.head(newMember, "<yellow><i>${newMember.name}".miniMsg(), isCenterOfInv = true, isLarge = true))
}

@Composable
fun GuildUIScope.AcceptGuildRequestButton(modifier: Modifier, newMember: OfflinePlayer) = Button(
    onClick = {
        if (player.getGuildJoinType() == GuildJoinType.INVITE) {
            player.error("Your guild is in 'INVITE-only' mode.")
            player.error("Change it to 'ANY' or 'REQUEST-only' mode to accept requests.")
            return@Button
        }
        if (!player.addMemberToGuild(newMember))  return@Button player.error("Failed to add ${newMember.name} to guild.")
        newMember.removeGuildQueueEntries(GuildJoinType.REQUEST)
        if (player.getGuildMemberCount() < guildLevel * 5) {
            newMember.removeGuildQueueEntries(GuildJoinType.REQUEST)
        }
        nav.back()
    },
    modifier = modifier
) {
    Text("<green>Accept GuildJoin-REQUEST".miniMsg(), modifier = Modifier.size(3, 3))
}

@Composable
fun GuildUIScope.DeclineGuildRequestButton(modifier: Modifier, newMember: OfflinePlayer) = Button(
    modifier = modifier,
    onClick = {
        guildName?.removeGuildQueueEntries(newMember, GuildJoinType.REQUEST)
        player.info("<yellow><b>❌ <yellow>You denied the join-request from ${newMember.name}")
        val requestDeniedMessage =
            "<red>Your request to join <i>${guildName} has been denied!"
        if (newMember.isOnline) newMember.player?.error(requestDeniedMessage)
        else {
            transaction(abyss.db) {
                GuildMessageQueue.insert {
                    it[content] = requestDeniedMessage
                    it[playerUUID] = newMember.uniqueId
                }
            }
        }
        nav.back()
        if (player.getNumberOfGuildRequests() == 0)
            nav.back()
    }
) {
    Text("<red>Decline GuildJoin-REQUEST".miniMsg(), modifier = Modifier.size(3, 3))
}

@Composable
fun GuildUIScope.DeclineAllGuildRequestsButton(modifier: Modifier) = Button(
    modifier = modifier,
    onClick = {
        player.removeGuildQueueEntries(GuildJoinType.REQUEST, true)
        player.info("<yellow><b>❌ <yellow>You denied all join-requests for your guild!")
        nav.back()
    }
) {
    Text("<red>Decline All Requests".miniMsg())
}
