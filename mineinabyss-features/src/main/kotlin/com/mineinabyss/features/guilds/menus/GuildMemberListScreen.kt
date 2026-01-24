package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mineinabyss.features.guilds.database.GuildJoinType
import com.mineinabyss.features.guilds.extensions.GuildDialogs
import com.mineinabyss.features.guilds.extensions.changeGuildJoinType
import com.mineinabyss.features.guilds.extensions.getGuildJoinType
import com.mineinabyss.features.guilds.extensions.getGuildMembers
import com.mineinabyss.features.guilds.extensions.getNumberOfGuildRequests
import com.mineinabyss.features.guilds.extensions.hasGuildRequest
import com.mineinabyss.features.guilds.extensions.invitePlayerToGuild
import com.mineinabyss.features.guilds.extensions.isCaptainOrAbove
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.VerticalGrid
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.components.canvases.MAX_CHEST_HEIGHT
import com.mineinabyss.guiy.components.lists.NavbarPosition
import com.mineinabyss.guiy.components.lists.ScrollDirection
import com.mineinabyss.guiy.components.lists.Scrollable
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.nms.entities.title
import com.mineinabyss.idofront.resourcepacks.ResourcePacks
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.registry.data.dialog.input.DialogInput
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack

@Composable
fun GuildUIScope.GuildMemberListScreen(
    onNavigateToMemberOptions: (member: OfflinePlayer) -> Unit,
    onNavigateToJoinRequests: () -> Unit,
) = Chest(":space_-8:${DecideMenus.decideMemberMenu(player, player.getGuildJoinType())}", Modifier.height((guildLevel + 2).coerceAtMost(MAX_CHEST_HEIGHT))) {
    var line by remember { mutableStateOf(0) }
    val guildMembers = remember { player.getGuildMembers().sortedWith(compareBy { it.player.isConnected; it.player.name; it.rank.ordinal }) }

    Scrollable(
        guildMembers, line,
        onLineChange = { line = it },
        nextButton = { ScrollDownButton() },
        previousButton = { ScrollUpButton(Modifier) },
        scrollDirection = ScrollDirection.VERTICAL,
        navbarPosition = NavbarPosition.END,
        navbarBackground = null
    ) { members ->
        VerticalGrid(Modifier.at(2, 1).size(5, minOf(guildLevel + 1, 4))) {
            members.forEach { (rank, member) ->
                Button(onClick = {
                    if (member != player && player.isCaptainOrAbove())
                        onNavigateToMemberOptions(member)
                }) {
                    Item(
                        TitleItem.head(
                            member, "<gold><i>${member.name}".miniMsg(),
                            "<yellow><b>Guild Rank: <yellow><i>$rank".miniMsg(),
                            isFlat = true
                        )
                    )
                }
            }
        }
    }

    BackButton(Modifier.at(0, minOf(guildLevel + 1, MAX_CHEST_HEIGHT - 1)))

    InviteToGuildButton(Modifier.at(7, 0))
    ManageGuildJoinRequestsButton(Modifier.at(8, 0), onNavigateToJoinRequests)
    ToggleGuildJoinTypeButton(Modifier.at(0, 0))
}

@Composable
fun ScrollDownButton(modifier: Modifier = Modifier) {
    Item(ItemStack.of(Material.PAPER).apply {
        setData(DataComponentTypes.ITEM_NAME, "<green><b>Scroll Down".miniMsg())
        setData(DataComponentTypes.ITEM_MODEL, ResourcePacks.EMPTY_MODEL)
    }, modifier)
}

@Composable
fun ScrollUpButton(modifier: Modifier = Modifier) {
    Item(ItemStack(Material.PAPER).apply {
        setData(DataComponentTypes.ITEM_NAME, "<blue><b>Scroll Up".miniMsg())
        setData(DataComponentTypes.ITEM_MODEL, ResourcePacks.EMPTY_MODEL)
    }, modifier)
}

@Composable
fun GuildUIScope.InviteToGuildButton(modifier: Modifier) {
    val guildInvitePaper = TitleItem.of("Player Name")
    guildInvitePaper.setData(DataComponentTypes.TOOLTIP_DISPLAY, TitleItem.hideTooltip)
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

            val dialog = GuildDialogs(
                ":space_-28::guild_search_menu:", "Send Guild-Invite!", listOf(
                    DialogInput.text("guild_dialog", "<gold>Search for Player to invite to your Guild...".miniMsg())
                        .initial("").width(200)
                        .maxLength(64)
                        .build()
                )
            ).createGuildLookDialog { player.invitePlayerToGuild(it) }

            player.showDialog(dialog)
        }
    ) {
        Text("<yellow><b>INVITE Player to Guild".miniMsg())
    }
}

@Composable
private fun GuildUIScope.ManageGuildJoinRequestsButton(
    modifier: Modifier,
    onNavigateToJoinRequests: () -> Unit,
) {
    val requestAmount = player.getNumberOfGuildRequests()
    val plural = requestAmount != 1
    Button(
        enabled = player.hasGuildRequest(),
        /* Icon that notifies player there are new invites */
        modifier = modifier,
        onClick = {
            if (player.isCaptainOrAbove()) {
                onNavigateToJoinRequests()
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
    val item = ItemStack(Material.PAPER).apply {
        setData(DataComponentTypes.ITEM_MODEL, ResourcePacks.EMPTY_MODEL)
        setData(DataComponentTypes.ITEM_NAME, "<dark_green><b>Toggle Guild GuildJoin Type".miniMsg())
        setData(DataComponentTypes.LORE, ItemLore.lore(listOf("<yellow>Currently players can join via:<gold><i> ${joinType.name}".miniMsg())))
    }
    Button(
        modifier = modifier,
        onClick = {
            if (!player.isCaptainOrAbove()) return@Button
            joinType = player.changeGuildJoinType()
            player.openInventory.title(":space_-8:${DecideMenus.decideMemberMenu(player, joinType)}".miniMsg())
        }
    ) {
        Item(item)
    }
}
