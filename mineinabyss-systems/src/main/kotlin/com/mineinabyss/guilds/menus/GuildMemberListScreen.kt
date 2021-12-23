package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.head
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.mineinabyss.core.AbyssContext
import com.mineinabyss.mineinabyss.data.GuildJoinType
import com.mineinabyss.mineinabyss.data.Players
import com.mineinabyss.mineinabyss.extensions.*
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
            Button(
                member.head(
                    "$GOLD$ITALIC${member.name}",
                    "$YELLOW${BOLD}Guild Rank: $YELLOW$ITALIC${member.getGuildRank()}",
                ),
                Modifier.clickable {
                    nav.open(GuildScreen.MemberOptions(member))
                }
            )
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
        TitleItem.of("${YELLOW}Invite Player to Guild"),
        modifier.size(1, 1).clickable {
            nav.open(GuildScreen.TextInput(
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
    )
}

@Composable
fun GuildUIScope.ManageGuildJoinRequestsButton(modifier: Modifier) {
    val requestAmount = player.getNumberOfGuildRequests()
    val plural = requestAmount != 1
    if (player.hasGuildRequest()) {
        Button(
            TitleItem.of(
                "${DARK_GREEN}Manage Guild Join Requests",
                "$YELLOW${ITALIC}There ${if (plural) "are" else "is"} currently $GOLD$BOLD$requestAmount ",
                "$YELLOW${ITALIC}join-request${if (plural) "s" else ""} for your guild."
            ),
            /* Icon that notifies player there are new invites */
            modifier.clickable {
                nav.open(GuildScreen.JoinRequestList)
            }
        )
    } else {
        Button(
            TitleItem.of(
                "$DARK_GREEN${STRIKETHROUGH}Manage Guild Join Requests",
                "$RED${ITALIC}There are currently no ",
                "$RED${ITALIC}join-requests for your guild."
            )
        )
    }

}

@Composable
fun GuildUIScope.ToggleGuildJoinTypeButton(modifier: Modifier) {
    val joinType by derivedStateOf { player.getGuildJoinType() }
    Button(
        TitleItem.of(
            "${DARK_GREEN}Toggle Guild Join Type",
            "${YELLOW}Currently players can join via: $GOLD$ITALIC$joinType",
        ),
        /* Custom Icon for "darkerened" out icon indicating no invites */
        modifier.clickable {
            player.changeGuildJoinType()
        }
    )
}
