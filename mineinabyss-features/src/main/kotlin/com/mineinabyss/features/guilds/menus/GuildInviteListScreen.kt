package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.database.GuildJoinQueue
import com.mineinabyss.features.guilds.database.GuildJoinType
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.head
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

@Composable
fun GuildUIScope.GuildInviteListScreen() {
    GuildInvites(Modifier.at(1, 1))
    DenyAllInvitesButton(Modifier.at(8, 4))
    BackButton(Modifier.at(2, 4))
}

@Composable
fun GuildUIScope.GuildInvites(modifier: Modifier = Modifier) {
    /* Transaction to query GuildInvites and playerUUID */
    val owner = player.getGuildOwnerFromInvite().toOfflinePlayer()
    val memberCount = owner.getGuildMemberCount()
    val invites = transaction(abyss.db) {
        GuildJoinQueue.selectAll().where {
            (GuildJoinQueue.joinType eq GuildJoinType.INVITE) and
                    (GuildJoinQueue.playerUUID eq player.uniqueId)
        }.map { row -> Invite(memberCount, row[GuildJoinQueue.guildId]) }

    }
    Grid(modifier.size(9, 4)) {
        invites.sortedBy { it.memberCount }.forEach { _ ->
            Button(onClick = {
                nav.open(GuildScreen.Invite(owner))
            }) {
                Item(player.head(
                    "<gold><b>Guildname: <yellow><i>${owner.getGuildName()}".miniMsg(),
                    "<blue>Click this to accept or deny invite.".miniMsg(),
                    "<blue>Info about the guild can also be found in here.".miniMsg(),
                    isFlat = true
                ))
            }
        }
    }
}

@Composable
fun GuildUIScope.DenyAllInvitesButton(modifier: Modifier) = Button(
    onClick = {
        player.removeGuildQueueEntries(GuildJoinType.INVITE, true)
        nav.open(GuildScreen.MemberList(guildLevel, player))
        player.info("<gold><b>❌<yellow>You denied all invites!")
    },
    modifier = modifier
) {
    Text("<red>Decline All Invites".miniMsg())
}

private class Invite(val memberCount: Int, val guildIds: Int)
