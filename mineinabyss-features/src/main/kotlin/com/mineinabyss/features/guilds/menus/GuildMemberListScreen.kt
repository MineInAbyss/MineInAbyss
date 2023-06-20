package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.*
import com.mineinabyss.features.guilds.database.GuildJoinType
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.guilds.menus.JoinTypeIcon.any
import com.mineinabyss.features.guilds.menus.JoinTypeIcon.invite
import com.mineinabyss.features.guilds.menus.JoinTypeIcon.request
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.features.helpers.head
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.MAX_CHEST_HEIGHT
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.navigation.UniversalScreens
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

@Composable
fun GuildUIScope.GuildMemberListScreen() {
    ManageGuildMembersButton(Modifier.at(2, 1))
    BackButton(Modifier.at(0, minOf(guildLevel + 1, MAX_CHEST_HEIGHT - 1)))

    InviteToGuildButton(Modifier.at(7, 0))
    ManageGuildJoinRequestsButton(Modifier.at(8, 0))
    ToggleGuildJoinTypeButton(Modifier.at(0, 0))
}

@Composable
fun GuildUIScope.ManageGuildMembersButton(modifier: Modifier) {
    Grid(modifier.size(5, guildLevel)) {
        player.getGuildMembers().sortedWith(compareBy { it.player.name; it.rank.ordinal }).forEach { (rank, member) ->
            Button(onClick = {
                if (member != player && player.isCaptainOrAbove())
                    nav.open(GuildScreen.MemberOptions(member))
            }) {
                Item(
                    member.head(
                        "<gold><i>${member.name}".miniMsg(),
                        "<yellow><b>Guild Rank: <yellow><i>$rank".miniMsg(),
                        isFlat = true
                    )
                )
            }
        }
    }
}

@Composable
fun GuildUIScope.InviteToGuildButton(modifier: Modifier) {
    val guildInvitePaper = TitleItem.of("Player Name")
    Button(
        enabled = player.isCaptainOrAbove(),
        modifier = modifier,
        onClick = {
            //if (player.isAboveCaptain()) return@Button
            if (player.getGuildJoinType() == GuildJoinType.REQUEST) {
                player.error("Your guild is in 'REQUEST-only' mode.")
                player.error("Change it to 'ANY' or 'INVITE-only' mode to invite others.")
                return@Button
            }
            nav.open(
                UniversalScreens.Anvil(
                AnvilGUI.Builder()
                    .title("${Space.of(-61)}:guild_search_menu:")
                    .itemLeft(guildInvitePaper)
                    .plugin(guiyPlugin)
                    .onClose { nav.back() }
                    .onClick { _, snapshot ->
                        snapshot.player.invitePlayerToGuild(snapshot.text)
                        listOf(AnvilGUI.ResponseAction.close())
                    }
            ))
        }
    ) {
        Text("<yellow><b>INVITE Player to Guild".miniMsg())
    }
}

@Composable
private fun GuildUIScope.ManageGuildJoinRequestsButton(modifier: Modifier) {
    val requestAmount = player.getNumberOfGuildRequests()
    val plural = requestAmount != 1
    Button(
        enabled = player.hasGuildRequest(),
        /* Icon that notifies player there are new invites */
        modifier = modifier,
        onClick = {
            if (player.isCaptainOrAbove()) {
                nav.open(GuildScreen.JoinRequestList)
            }
        }
    ) { enabled ->
        if (enabled) Text(
            "<dark_green><b>Manage Guild GuildJoin Requests".miniMsg(),
            "<yellow><i>There ${if (plural) "are" else "is"} currently <gold><b>$requestAmount ".miniMsg(),
            "<yellow><i>join-request${if (plural) "s" else ""} for your guild.".miniMsg()
        )
        else Text(
            "<dark_green><b><st>Manage Guild GuildJoin Requests".miniMsg(),
            "<red><i>There are currently no ".miniMsg(),
            "<red><i>join-requests for your guild.".miniMsg()
        )
    }

}

@Composable
private fun GuildUIScope.ToggleGuildJoinTypeButton(modifier: Modifier) {
    var joinType by remember { mutableStateOf(player.getGuildJoinType()) }
    val item = if (joinType == GuildJoinType.ANY) any else if (joinType == GuildJoinType.INVITE) invite else request
    Button(
        modifier = modifier,
        onClick = {
            if (player.isCaptainOrAbove()) {
                player.changeGuildJoinType()
                joinType = player.getGuildJoinType()
            }
        }
    ) {
        Item(item)
    }
}

private object JoinTypeIcon {
    val any = ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(4)
        displayName("<dark_green><b>Toggle Guild GuildJoin Type".miniMsg())
        lore(listOf("<yellow>Currently players can join via:<gold><i> ANY".miniMsg()))
    }

    val invite = ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(5)
        displayName("<dark_green><b>Toggle Guild GuildJoin Type".miniMsg())
        lore(listOf("<yellow>Currently players can join via:<gold><i> INVITE".miniMsg()))
    }

    val request = ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(6)
        displayName("<dark_green><b>Toggle Guild GuildJoin Type".miniMsg())
        lore(listOf("<yellow>Currently players can join via:<gold><i> REQUEST".miniMsg()))
    }
}
