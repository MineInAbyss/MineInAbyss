package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
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
import com.mineinabyss.guiy.components.canvases.Chest
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
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

@Composable
fun GuildUIScope.GuildJoinRequestListScreen(
    onNavigateToJoinRequest: (newMember: OfflinePlayer) -> Unit,
    onBack: () -> Unit,
) = Chest(":space_-8::guild_inbox_list_menu:", Modifier.height(5.dp)) {
    GuildJoinRequestButton(Modifier.offset(1.dp, 0.dp), onNavigateToJoinRequest)
    DeclineAllGuildRequestsButton(Modifier.offset(8.dp, 4.dp), onBack)
    BackButton(Modifier.offset(2.dp, 4.dp))
}

@Composable
fun GuildUIScope.GuildJoinRequestButton(
    modifier: Modifier = Modifier,
    onNavigateToJoinRequest: (newMember: OfflinePlayer) -> Unit,
) {
    /* Transaction to query GuildInvites and playerUUID */
    val requests = remember {
        transaction(abyss.db) {
            val id = Players.selectAll().where { Players.playerUUID eq player.uniqueId.toKotlinUuid() }.first()[Players.guildId]

            GuildJoinQueue.selectAll()
                .where { (GuildJoinQueue.guildId eq id) and (GuildJoinQueue.joinType eq GuildJoinType.REQUEST) }
                .map { row -> row[GuildJoinQueue.playerUUID] }
        }
    }
    HorizontalGrid(modifier.size(8.dp, 4.dp)) {
        requests.map { it.toJavaUuid().toOfflinePlayer() }.forEach { newMember ->
            Button(onClick = {
                if (!player.hasGuildRequests()) owner.exit()
                else onNavigateToJoinRequest(newMember)
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
