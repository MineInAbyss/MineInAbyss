package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.database.GuildJoinQueue
import com.mineinabyss.features.guilds.database.GuildJoinType
import com.mineinabyss.features.guilds.database.Players
import com.mineinabyss.features.guilds.extensions.hasGuildRequests
import com.mineinabyss.features.guilds.extensions.toOfflinePlayer
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.HorizontalGrid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

@Composable
fun GuildUIScope.GuildJoinRequestListScreen() {
    GuildJoinRequestButton(Modifier.at(1, 1))
    DeclineAllGuildRequestsButton(Modifier.at(8, 4))
    BackButton(Modifier.at(2, 4))
}

@Composable
fun GuildUIScope.GuildJoinRequestButton(modifier: Modifier = Modifier) {
    /* Transaction to query GuildInvites and playerUUID */
    val requests = remember {
        transaction(abyss.db) {
            val id = Players.selectAll().where { Players.playerUUID eq player.uniqueId }.first()[Players.guildId]

            GuildJoinQueue.selectAll()
                .where { (GuildJoinQueue.guildId eq id) and (GuildJoinQueue.joinType eq GuildJoinType.REQUEST) }
                .map { row -> row[GuildJoinQueue.playerUUID] }
        }
    }
    HorizontalGrid(modifier.size(9, 4)) {
        requests.map { it.toOfflinePlayer() }.forEach { newMember ->
            Button(onClick = {
                if (!player.hasGuildRequests()) player.closeInventory()
                else nav.open(GuildScreen.JoinRequest(newMember))
            }) {
                Item(
                    TitleItem.head(
                        newMember, "<yellow><i>${newMember.name}".miniMsg(),
                        "<blue>Click this to accept or deny the join-request.".miniMsg(),
                        isFlat = true
                    )
                )
            }
        }
    }
}
