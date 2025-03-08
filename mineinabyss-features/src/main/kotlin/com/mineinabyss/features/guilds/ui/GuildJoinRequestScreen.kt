package com.mineinabyss.features.guilds.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.mineinabyss.features.guilds.data.tables.GuildJoinType
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.PlayerHead
import com.mineinabyss.guiy.components.items.PlayerHeadType
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.OfflinePlayer

@Composable
fun GuildJoinRequestScreen(from: OfflinePlayer) {
    PlayerLabel(Modifier.at(4, 0), from)
    AcceptGuildRequestButton(Modifier.at(1, 1), from)
    DeclineGuildRequestButton(Modifier.at(5, 1), from)
    BackButton(Modifier.at(4, 4))
}

@Composable
fun PlayerLabel(modifier: Modifier, newMember: OfflinePlayer) = Button(modifier = modifier) {
    PlayerHead(newMember, "<yellow><i>${newMember.name}", type = PlayerHeadType.LARGE_CENTER)
}

@Composable
fun AcceptGuildRequestButton(modifier: Modifier, newMember: OfflinePlayer) {
    val canAccept = canAcceptNewMembers.collectAsState().value
    Button(
        onClick = {
            //TODO
//            if (player.getGuildJoinType() == GuildJoinType.INVITE) {
//                player.error("Your guild is in 'INVITE-only' mode.")
//                player.error("Change it to 'ANY' or 'REQUEST-only' mode to accept requests.")
//                return@Button
//            }
//            if (!player.addMemberToGuild(newMember)) return@Button player.error("Failed to add ${newMember.name} to guild.")
//            newMember.removeGuildQueueEntries(GuildJoinType.REQUEST)
//            if (canAccept) {
//                newMember.removeGuildQueueEntries(GuildJoinType.REQUEST)
//            }
//            nav.back()
        },
        modifier = modifier
    ) {
        Text("<green>Accept GuildJoin-REQUEST".miniMsg(), modifier = Modifier.size(3, 3))
    }
}

@Composable
fun DeclineGuildRequestButton(modifier: Modifier, newMember: OfflinePlayer) = Button(
    modifier = modifier,
    onClick = {
        //TODO
//        guildName?.removeGuildQueueEntries(newMember, GuildJoinType.REQUEST)
//        player.info("<yellow><b>❌ <yellow>You denied the join-request from ${newMember.name}")
//        val requestDeniedMessage =
//            "<red>Your request to join <i>${guildName} has been denied!"
//        if (newMember.isOnline) newMember.player?.error(requestDeniedMessage)
//        else {
//            transaction(abyss.db) {
//                GuildMessageQueue.insert {
//                    it[content] = requestDeniedMessage
//                    it[playerUUID] = newMember.uniqueId
//                }
//            }
//        }
//        nav.back()
//        if (player.getNumberOfGuildRequests() == 0)
//            nav.back()
    }
) {
    Text("<red>Decline GuildJoin-REQUEST".miniMsg(), modifier = Modifier.size(3, 3))
}
