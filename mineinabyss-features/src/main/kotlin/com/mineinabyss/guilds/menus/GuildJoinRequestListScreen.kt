package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mineinabyss.guilds.database.GuildJoinQueue
import com.mineinabyss.guilds.database.GuildJoinType
import com.mineinabyss.guilds.database.Players
import com.mineinabyss.guilds.extensions.hasGuildRequests
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.head
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.messaging.miniMsg
import com.mineinabyss.mineinabyss.core.AbyssContext
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

@Composable
fun GuildUIScope.GuildJoinRequestListScreen() {
    GuildJoinRequestButton(Modifier.at(1, 1))
    DenyAllInvitesButton(Modifier.at(8, 4))
    BackButton(Modifier.at(2, 4))
}

@Composable
fun GuildUIScope.GuildJoinRequestButton(modifier: Modifier = Modifier) {
    /* Transaction to query GuildInvites and playerUUID */
    val requests = remember {
        transaction(AbyssContext.db) {
            val id = Players.select {
                Players.playerUUID eq player.uniqueId
            }.first()[Players.guildId]

            GuildJoinQueue.select {
                (GuildJoinQueue.guildId eq id) and (GuildJoinQueue.joinType eq GuildJoinType.REQUEST)
            }.map { row -> row[GuildJoinQueue.playerUUID] }
        }
    }
    Grid(modifier.size(9, 4)) {
        requests.forEach { newMember ->
            Button(onClick = {
                if (!player.hasGuildRequests()) player.closeInventory()
                else nav.open(GuildScreen.JoinRequest(Bukkit.getOfflinePlayer(newMember)))
            }) {
                Item(
                    newMember.toPlayer().head(
                        "<yellow><i>${newMember.toPlayer()?.name}".miniMsg(),
                        "<blue>Click this to accept or deny the join-request.".miniMsg(),
                        isFlat = true
                    )
                )
            }
        }
    }
    DeclineAllGuildRequestsButton(Modifier.at(8, 4))

    BackButton(Modifier.at(2, 4))
}
