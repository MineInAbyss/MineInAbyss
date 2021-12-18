package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.nodes.InventoryCanvasScope.at
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.mineinabyss.core.AbyssContext
import com.mineinabyss.mineinabyss.data.GuildJoinQueue
import com.mineinabyss.mineinabyss.data.GuildJoinType
import com.mineinabyss.mineinabyss.extensions.getGuildMemberCount
import com.mineinabyss.mineinabyss.extensions.getGuildName
import com.mineinabyss.mineinabyss.extensions.getGuildOwnerFromInvite
import com.mineinabyss.mineinabyss.extensions.removeGuildQueueEntries
import org.bukkit.ChatColor.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

@Composable
fun GuildUIScope.GuildInviteListScreen() {
    GuildInvites(Modifier.at(1, 1))
    DenyAllInvites(Modifier.at(8, 3))
    BackButton(Modifier.at(2, 3))
}

@Composable
fun GuildUIScope.GuildInvites(modifier: Modifier = Modifier) {
    /* Transaction to query GuildInvites and playerUUID */
    val owner = player.getGuildOwnerFromInvite().toPlayer()!!
    val memberCount = owner.getGuildMemberCount()
    val invites = transaction(AbyssContext.db) {
        GuildJoinQueue.select {
            (GuildJoinQueue.joinType eq GuildJoinType.Invite) and
                    (GuildJoinQueue.playerUUID eq player.uniqueId)
        }.map { row -> Pair(memberCount, row[GuildJoinQueue.guildId]) }

    }
    Grid(modifier.size(9, 4)) {
        //TODO instead of using a Pair, create a private class and name first/second properly
        invites.sortedBy { it.first }.forEach { (memberCount, guild) ->
            val guildItem = TitleItem.of(
                "$GOLD${BOLD}Guildname: $YELLOW$ITALIC${owner.getGuildName()}",
                "${BLUE}Click this to accept or deny invite.",
                "${BLUE}Info about the guild can also be found in here."
            )
            Button(guildItem, Modifier.clickable {
                //TODO get guild from guild param above
                nav.open(GuildScreen.Invite(player.getGuildOwnerFromInvite().toPlayer()!!))
            })
        }
    }
}

@Composable
fun GuildUIScope.DenyAllInvites(modifier: Modifier) = Button(
    TitleItem.of("${RED}Decline All Invites"),
    modifier.clickable {
        player.removeGuildQueueEntries(GuildJoinType.Invite, true)
        nav.open(GuildScreen.MemberList(guildLevel))
        player.sendMessage("$YELLOW${BOLD}❌${YELLOW}You denied all invites!")
    }
)
