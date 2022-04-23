package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guilds.database.GuildRanks
import com.mineinabyss.guilds.database.Players
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.head
import com.mineinabyss.mineinabyss.core.AbyssContext
import com.mineinabyss.mineinabyss.extensions.getGuildRank
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction


@Composable
fun GuildUIScope.CurrentGuildScreen() {
    val yLevel = guildLevel + 2
    GuildMemberList(Modifier.at(1, 1))
    if (player.getGuildRank() != GuildRanks.Owner) {
        //LeaveGuildButton(player, Modifier.at(8, yLevel))
    }
    BackButton(Modifier.at(2, yLevel))
}


@Composable
fun GuildUIScope.GuildMemberList(modifier: Modifier) {
    val members = transaction(AbyssContext.db) {
        val players = Players
            .select { Players.playerUUID eq player.uniqueId }
            .single()

        val guildId = players[Players.guildId]

        Players
            .select { Players.guildId eq guildId }
            .map { row -> Pair(row[Players.guildRank], Bukkit.getOfflinePlayer(row[Players.playerUUID])) }
    }

    Grid(modifier.size(5, guildLevel)) {
        members.sortedBy { it.first }.forEach { (rank, member) ->
            Item(
                member.head(
                    "$GOLD$ITALIC${member.name}",
                    "$YELLOW${BOLD}Guild Rank: $YELLOW$ITALIC${member.getGuildRank()}",
                    isFlat = true
                )
            )
        }
    }
}
