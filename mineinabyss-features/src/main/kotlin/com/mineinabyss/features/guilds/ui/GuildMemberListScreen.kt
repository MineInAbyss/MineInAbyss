package com.mineinabyss.features.guilds.ui

import androidx.compose.runtime.*
import com.mineinabyss.features.guilds.data.tables.GuildJoinType
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.VerticalGrid
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.canvases.MAX_CHEST_HEIGHT
import com.mineinabyss.guiy.components.items.PlayerHead
import com.mineinabyss.guiy.components.items.PlayerHeadType
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.components.lists.NavbarPosition
import com.mineinabyss.guiy.components.lists.ScrollDirection
import com.mineinabyss.guiy.components.lists.Scrollable
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.click.clickable
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.navigation.UniversalScreens
import com.mineinabyss.idofront.entities.title
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

@Composable
fun GuildViewModel.GuildMemberListScreen() {
    var line by remember { mutableStateOf(0) }
    val guildMembers = remember {
        player.getGuildMembers().sortedWith(compareBy { it.player.isConnected; it.player.name; it.rank.ordinal })
    }
    val isCaptain = isCaptainOrAbove.collectAsState().value
    val guild = currentGuild.collectAsState().value
    if (guild == null) return

    Scrollable(
        guildMembers, line, ScrollDirection.VERTICAL,
        nextButton = { ScrollDownButton(Modifier.at(0, 3).clickable { line++ }) },
        previousButton = { ScrollUpButton(Modifier.at(0, 1).clickable { line-- }) },
        NavbarPosition.START, null
    ) { members ->
        VerticalGrid(Modifier.at(1, 1).size(5, minOf(guild.level + 1, 4))) {
            members.forEach { (rank, member) ->
                Button(onClick = {
                    if (member != player && isCaptain)
                        nav.open(GuildScreen.MemberOptions(member))
                }) {
                    PlayerHead(
                        member, "<gold><i>${member.name}",
                        "<yellow><b>Guild Rank: <yellow><i>$rank",
                        type = PlayerHeadType.FLAT
                    )
                }
            }
        }
    }

    BackButton(Modifier.at(0, minOf(guild.level + 1, MAX_CHEST_HEIGHT - 1)))

    InviteToGuildButton(Modifier.at(7, 0))
    ManageGuildJoinRequestsButton(Modifier.at(8, 0))
    ToggleGuildJoinTypeButton(Modifier.at(0, 0))
}

@Composable
fun ScrollDownButton(modifier: Modifier = Modifier) {
    Item(ItemStack(Material.PAPER).editItemMeta {
        itemName("<green><b>Scroll Down".miniMsg())
        setCustomModelData(1)
    }, modifier)
}

@Composable
fun ScrollUpButton(modifier: Modifier = Modifier) {
    Item(ItemStack(Material.PAPER).editItemMeta {
        itemName("<blue><b>Scroll Up".miniMsg())
        setCustomModelData(1)
    }, modifier)
}

@Composable
fun GuildViewModel.InviteToGuildButton(modifier: Modifier) {
    val guildInvitePaper = TitleItem.of("Player Name").editItemMeta { isHideTooltip = true }
    val isCaptain = isCaptainOrAbove.collectAsState().value
    Button(
        enabled = isCaptain,
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
                        .title(":space_-61::guild_search_menu:")
                        .itemLeft(guildInvitePaper)
                        .itemOutput(TitleItem.transparentItem)
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
private fun GuildViewModel.ManageGuildJoinRequestsButton(modifier: Modifier) {
    val requestAmount = player.getNumberOfGuildRequests()
    val plural = requestAmount != 1
    val isCaptain = isCaptainOrAbove.collectAsState().value
    Button(
        enabled = player.hasGuildRequest() && isCaptain,
        /* Icon that notifies player there are new invites */
        modifier = modifier,
        onClick = {
            nav.open(GuildScreen.JoinRequestList)
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
private fun GuildViewModel.ToggleGuildJoinTypeButton(modifier: Modifier) {
    var joinType by remember { mutableStateOf(player.getGuildJoinType()) }
    val item = ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(1)
        itemName("<dark_green><b>Toggle Guild GuildJoin Type".miniMsg())
        lore(listOf("<yellow>Currently players can join via:<gold><i> ${joinType.name}".miniMsg()))
    }
    val isCaptain = isCaptainOrAbove.collectAsState().value
    Button(
        enabled = isCaptain,
        modifier = modifier,
        onClick = {
            joinType = player.changeGuildJoinType()
            player.openInventory.title(":space_-8:${DecideMenus.decideMemberMenu(player, joinType)}".miniMsg())
        }
    ) {
        Item(item)
    }
}
