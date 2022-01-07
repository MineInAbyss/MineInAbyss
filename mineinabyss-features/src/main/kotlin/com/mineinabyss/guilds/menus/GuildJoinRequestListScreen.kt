package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.head
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.mineinabyss.core.AbyssContext
import com.mineinabyss.mineinabyss.data.GuildJoinQueue
import com.mineinabyss.mineinabyss.data.GuildJoinType
import com.mineinabyss.mineinabyss.data.Players
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

@Composable
fun GuildUIScope.GuildJoinRequestListScreen() {
    /* Transaction to query GuildInvites and playerUUID */
    val requests = remember {
        transaction(AbyssContext.db) {
            val id = Players.select {
                Players.playerUUID eq player.uniqueId
            }.first()[Players.guildId]

            GuildJoinQueue.select {
                (GuildJoinQueue.guildId eq id) and (GuildJoinQueue.joinType eq GuildJoinType.Request)
            }.map { row -> row[GuildJoinQueue.playerUUID] }
        }
    }
    Grid(Modifier.size(9, 4)) {
        requests.forEach { newMember ->
            Button(onClick = { nav.open(GuildScreen.JoinRequest(Bukkit.getOfflinePlayer(newMember))) }) {
                Item(
                    newMember.toPlayer().head(
                        "$YELLOW$ITALIC${newMember.toPlayer()?.name}",
                        "${BLUE}Click this to accept or deny the join-request."
                    )
                )
            }
        }
    }
    DeclineAllGuildRequests(Modifier.at(8, 4))

    BackButton(Modifier.at(2, 4))
}
