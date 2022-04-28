package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import com.mineinabyss.guilds.database.GuildJoinType
import com.mineinabyss.guilds.database.Players
import com.mineinabyss.guilds.extensions.*
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.head
import com.mineinabyss.helpers.ui.UniversalScreens
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.mineinabyss.core.AbyssContext
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

@Composable
fun GuildUIScope.GuildMemberListScreen() {
    ManageGuildMembersButton(Modifier.at(1, 1))
    InviteToGuildButton(Modifier.at(7, 0))
    ToggleGuildJoinTypeButton(Modifier.at(8, 1))
    ManageGuildJoinRequestsButton(Modifier.at(8, 0))
    BackButton(Modifier.at(2, guildLevel + 1))
}

@Composable
fun GuildUIScope.ManageGuildMembersButton(modifier: Modifier) {
    //TODO move transaction into helper function
    val players = transaction(AbyssContext.db) {
        val playerRow = Players.select {
            Players.playerUUID eq player.uniqueId
        }.single()

        val guildId = playerRow[Players.guildId]

        Players.select {
            (Players.guildId eq guildId) and
                    (Players.playerUUID neq player.uniqueId)
        }.map { row ->
            Pair(row[Players.guildRank], Bukkit.getOfflinePlayer(row[Players.playerUUID]))
        }
    }
    Grid(modifier.size(5, guildLevel)) {
        players.sortedBy { it.first }.forEach { (rank, member) ->
            Button(onClick = { nav.open(GuildScreen.MemberOptions(member)) }) {
                Item(
                    member.head(
                        "$GOLD$ITALIC${member.name}",
                        "$YELLOW${BOLD}Guild Rank: $YELLOW$ITALIC${member.getGuildRank()}",
                    )
                )
            }
        }
    }
}

@Composable
fun GuildUIScope.InviteToGuildButton(modifier: Modifier) {
    if (player.getGuildJoinType() == GuildJoinType.Request) {
        player.error("Your guild is in 'Request-only' mode.")
        player.error("Change it to 'Any' or 'Invite-only' mode to invite others.")
    }

    val guildInvitePaper = ItemStack(Material.PAPER).editItemMeta {
        setDisplayName("$BLUE${ITALIC}Playername")
        setCustomModelData(1)
    }
    Button(
        onClick = {
            nav.open(UniversalScreens.Anvil(
                AnvilGUI.Builder()
                    .title(":guild_invite:")
                    .itemLeft(guildInvitePaper)
                    .plugin(guiyPlugin)
                    .onClose { nav.back() }
                    .onComplete { player, invitedPlayer: String ->
                        player.invitePlayerToGuild(invitedPlayer)
                        AnvilGUI.Response.close()
                    }
            ))
        }
    ) {
        Text("${YELLOW}Invite Player to Guild")
    }
}

@Composable
fun GuildUIScope.ManageGuildJoinRequestsButton(modifier: Modifier) {
    val requestAmount = player.getNumberOfGuildRequests()
    val plural = requestAmount != 1
    Button(
        enabled = player.hasGuildRequest(),
        /* Icon that notifies player there are new invites */
        modifier = modifier,
        onClick = { nav.open(GuildScreen.JoinRequestList) }
    ) { enabled ->
        if (enabled) Text(
            "${DARK_GREEN}Manage Guild Join Requests",
            "$YELLOW${ITALIC}There ${if (plural) "are" else "is"} currently $GOLD$BOLD$requestAmount ",
            "$YELLOW${ITALIC}join-request${if (plural) "s" else ""} for your guild."
        )
        else Text(
            "$DARK_GREEN${STRIKETHROUGH}Manage Guild Join Requests",
            "$RED${ITALIC}There are currently no ",
            "$RED${ITALIC}join-requests for your guild."
        )
    }

}

@Composable
fun GuildUIScope.ToggleGuildJoinTypeButton(modifier: Modifier) {
    val joinType by derivedStateOf { player.getGuildJoinType() }
    Button(
        /* Custom Icon for "darkerened" out icon indicating no invites */
        modifier = modifier,
        onClick = { player.changeGuildJoinType() }
    ) {
        Text(
            "${DARK_GREEN}Toggle Guild Join Type",
            "${YELLOW}Currently players can join via: $GOLD$ITALIC$joinType",
        )
    }
}
