package com.mineinabyss.guilds.menus

import androidx.compose.runtime.*
import com.mineinabyss.guilds.database.GuildJoinType
import com.mineinabyss.guilds.database.GuildRanks
import com.mineinabyss.guilds.extensions.changeGuildJoinType
import com.mineinabyss.guilds.extensions.getGuildMembers
import com.mineinabyss.guilds.menus.JoinTypeIcon.any
import com.mineinabyss.guilds.menus.JoinTypeIcon.invite
import com.mineinabyss.guilds.menus.JoinTypeIcon.request
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.NoToolTip
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.head
import com.mineinabyss.helpers.ui.UniversalScreens
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.mineinabyss.extensions.*
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

@Composable
fun GuildUIScope.GuildMemberListScreen() {
    ManageGuildMembersButton(Modifier.at(1, 1))
    BackButton(Modifier.at(0, guildLevel + 1))

    if (player.getGuildRank() == GuildRanks.Owner || player.getGuildRank() == GuildRanks.Captain) {
        InviteToGuildButton(Modifier.at(7, 0))
        ManageGuildJoinRequestsButton(Modifier.at(8, 0))
        ToggleGuildJoinTypeButton(Modifier.at(0, 0))
    }
}

@Composable
fun GuildUIScope.ManageGuildMembersButton(modifier: Modifier) {
    Grid(modifier.size(5, guildLevel)) {
        player.getGuildMembers().sortedBy { it.first; it.second.name }.forEach { (rank, member) ->
            Button(onClick = { if (member != player) nav.open(GuildScreen.MemberOptions(member)) }) {
                Item(
                    member.head(
                        "$GOLD$ITALIC${member.name}",
                        "$YELLOW${BOLD}Guild Rank: $YELLOW$ITALIC${rank}",
                        isFlat = true
                    )
                )
            }
        }
    }
}

@Composable
fun GuildUIScope.InviteToGuildButton(modifier: Modifier) {
    val guildInvitePaper = TitleItem.of("Player Name").NoToolTip()
    Button(
        modifier = modifier,
        onClick = {
            if (player.getGuildJoinType() == GuildJoinType.Request) {
                player.error("Your guild is in 'Request-only' mode.")
                player.error("Change it to 'Any' or 'Invite-only' mode to invite others.")
                return@Button
            }
            nav.open(UniversalScreens.Anvil(
                AnvilGUI.Builder()
                    .title("${Space.of(-64)}${Space.of(1)}:guild_search_menu:")
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
    var joinType by remember { mutableStateOf(player.getGuildJoinType()) }
    val item = if (joinType == GuildJoinType.Any) any else if (joinType == GuildJoinType.Invite) invite else request
        Button(
            modifier = modifier,
            onClick = { player.changeGuildJoinType() ; joinType = player.getGuildJoinType() }
        ) {
            Item(item)
        }
}

object JoinTypeIcon {
    val any = ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(4)
        setDisplayName("${DARK_GREEN}Toggle Guild Join Type")
        lore = listOf("${YELLOW}Currently players can join via:$GOLD$ITALIC Any")
    }

    val invite = ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(5)
        setDisplayName("${DARK_GREEN}Toggle Guild Join Type")
        lore = listOf("${YELLOW}Currently players can join via:$GOLD$ITALIC Invite")
    }

    val request = ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(6)
        setDisplayName("${DARK_GREEN}Toggle Guild Join Type")
        lore = listOf("${YELLOW}Currently players can join via:$GOLD$ITALIC Request")
    }
}
