package com.mineinabyss.features.guilds.menus.screens.invites

import androidx.compose.runtime.Composable
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.database.GuildJoinQueue
import com.mineinabyss.features.guilds.database.GuildJoinType
import com.mineinabyss.features.guilds.extensions.getGuildMemberCount
import com.mineinabyss.features.guilds.extensions.getGuildName
import com.mineinabyss.features.guilds.extensions.getGuildOwnerFromInvite
import com.mineinabyss.features.guilds.extensions.toOfflinePlayer
import com.mineinabyss.features.guilds.menus.GuildScreen
import com.mineinabyss.features.guilds.menus.GuildViewModel
import com.mineinabyss.guiy.components.HorizontalGrid
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.PlayerHead
import com.mineinabyss.guiy.components.items.PlayerHeadType
import com.mineinabyss.guiy.inventory.CurrentPlayer
import com.mineinabyss.guiy.inventory.viewModel
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.size
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

private class Invite(val memberCount: Int, val guildIds: Int)

@Composable
fun GuildInvites(
    modifier: Modifier = Modifier,
    player: Player = CurrentPlayer,
    guild: GuildViewModel = viewModel(),
) {
    /* Transaction to query GuildInvites and playerUUID */
    val owner = player.getGuildOwnerFromInvite().toOfflinePlayer()
    val memberCount = owner.getGuildMemberCount()
    val invites = transaction(abyss.db) {
        GuildJoinQueue.selectAll().where {
            (GuildJoinQueue.joinType eq GuildJoinType.INVITE) and
                    (GuildJoinQueue.playerUUID eq player.uniqueId)
        }.map { row -> Invite(memberCount, row[GuildJoinQueue.guildId].value) }

    }
    HorizontalGrid(modifier.size(9, 4)) {
        invites.sortedBy { it.memberCount }.forEach { _ ->
            Button(onClick = { guild.nav.open(GuildScreen.Invite(owner)) }) {
                PlayerHead(
                    player,
                    "<gold><b>Guildname: <yellow><i>${owner.getGuildName()}",
                    "<blue>Click this to accept or deny invite.",
                    "<blue>Info about the guild can also be found in here.",
                    type = PlayerHeadType.FLAT,
                )
            }
        }
    }
}
