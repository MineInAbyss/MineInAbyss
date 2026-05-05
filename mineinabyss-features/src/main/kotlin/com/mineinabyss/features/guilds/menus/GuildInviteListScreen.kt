package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.database.GuildJoinQueue
import com.mineinabyss.features.guilds.database.GuildJoinType
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.HorizontalGrid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.textcomponents.miniMsg
import me.dvyy.compose.mini.layout.modifiers.height
import me.dvyy.compose.mini.layout.modifiers.offset
import me.dvyy.compose.mini.layout.modifiers.size
import me.dvyy.compose.mini.modifier.Modifier
import org.bukkit.OfflinePlayer
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.toKotlinUuid

@Composable
fun GuildUIScope.GuildInviteListScreen(
    onNavigateToInviteScreen: (owner: OfflinePlayer) -> Unit,
    onNavigateToMemberList: () -> Unit,
) = Chest(":space_-8::guild_inbox_list_menu:", Modifier.height(5.dp)) {
    GuildInvites(Modifier.offset(1.dp, 1.dp), onNavigateToInviteScreen)
    DenyAllInvitesButton(Modifier.offset(8.dp, 4.dp), onNavigateToMemberList)
    BackButton(Modifier.offset(2.dp, 4.dp))
}

@Composable
fun GuildUIScope.GuildInvites(modifier: Modifier = Modifier, onNavigateToInviteScreen: (owner: OfflinePlayer) -> Unit) {
    /* Transaction to query GuildInvites and playerUUID */
    val owner = player.getGuildOwnerFromInvite().toOfflinePlayer()
    val memberCount = owner.getGuildMemberCount()
    val invites = transaction(abyss.db) {
        GuildJoinQueue.selectAll().where {
            (GuildJoinQueue.joinType eq GuildJoinType.INVITE) and
                    (GuildJoinQueue.playerUUID eq player.uniqueId.toKotlinUuid())
        }.map { row -> Invite(memberCount, row[GuildJoinQueue.guildId]) }

    }
    HorizontalGrid(modifier.size(9.dp, 4.dp)) {
        invites.sortedBy { it.memberCount }.forEach { _ ->
            Button(onClick = { onNavigateToInviteScreen(owner) }) {
                Item(
                    TitleItem.head(
                        player.uniqueId, "<gold><b>Guildname: <yellow><i>${owner.getGuildName()}".miniMsg(),
                        "<blue>Click this to accept or deny invite.".miniMsg(),
                        "<blue>Info about the guild can also be found in here.".miniMsg(),
                        isFlat = true
                    )
                )
            }
        }
    }
}

@Composable
fun GuildUIScope.DenyAllInvitesButton(
    modifier: Modifier,
    onNavigateToMemberList: () -> Unit,
) = Button(
    onClick = {
        player.removeGuildQueueEntries(GuildJoinType.INVITE, true)
        player.info("<gold><b>❌<yellow>You denied all invites!")
        onNavigateToMemberList()
    },
    modifier = modifier
) {
    Text("<red>Decline All Invites".miniMsg())
}

private class Invite(val memberCount: Int, val guildIds: Int)
