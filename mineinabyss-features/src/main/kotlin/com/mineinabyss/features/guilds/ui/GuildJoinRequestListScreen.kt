package com.mineinabyss.features.guilds.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.data.tables.GuildJoinRequestsTable
import com.mineinabyss.features.guilds.data.tables.GuildJoinType
import com.mineinabyss.features.guilds.data.tables.GuildMembersTable
import com.mineinabyss.features.guilds.extensions.hasGuildRequests
import com.mineinabyss.features.guilds.extensions.toOfflinePlayer
import com.mineinabyss.guiy.components.HorizontalGrid
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.PlayerHead
import com.mineinabyss.guiy.components.items.PlayerHeadType
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

@Composable
fun GuildViewModel.GuildJoinRequestListScreen() {
    GuildJoinRequestButton(Modifier.at(1, 1))
    DeclineAllGuildRequestsButton(Modifier.at(8, 4))
    BackButton(Modifier.at(2, 4))
}

@Composable
fun GuildViewModel.GuildJoinRequestButton(modifier: Modifier = Modifier) {
    /* Transaction to query GuildInvites and playerUUID */
    val requests = remember {
        transaction(abyss.db) {
            val id = GuildMembersTable.selectAll().where { GuildMembersTable.id eq player.uniqueId }.first()[GuildMembersTable.guild]

            GuildJoinRequestsTable.selectAll()
                .where { (GuildJoinRequestsTable.guildId eq id) and (GuildJoinRequestsTable.joinType eq GuildJoinType.REQUEST) }
                .map { row -> row[GuildJoinRequestsTable.playerUUID] }
        }
    }
    HorizontalGrid(modifier.size(9, 4)) {
        requests.map { it.value.toOfflinePlayer() }.forEach { newMember ->
            Button(onClick = {
                if (!player.hasGuildRequests()) player.closeInventory()
                else nav.open(GuildScreen.JoinRequest(newMember))
            }) {
                PlayerHead(
                    newMember,
                    "<yellow><i>${newMember.name}",
                    "<blue>Click this to accept or deny the join-request.",
                    type = PlayerHeadType.FLAT
                )
            }
        }
    }
}
