package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.data.GuildRepository
import com.mineinabyss.features.guilds.database.GuildJoinType
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.guilds.menus.GuildScreen.*
import com.mineinabyss.features.guilds.menus.screens.GuildDisbandScreen
import com.mineinabyss.features.guilds.menus.screens.GuildInfoScreen
import com.mineinabyss.features.guilds.menus.screens.HomeScreen
import com.mineinabyss.features.guilds.menus.screens.invites.GuildInviteListScreen
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.inventory.CurrentPlayer
import com.mineinabyss.guiy.inventory.LocalGuiyOwner
import com.mineinabyss.guiy.inventory.viewModel
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.navigation.Navigator
import com.mineinabyss.guiy.navigation.UniversalScreens
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import net.wesjd.anvilgui.AnvilGUI
import net.wesjd.anvilgui.AnvilGUI.ResponseAction
import org.bukkit.entity.Player

typealias GuildNav = Navigator<GuildScreen>

@Composable
fun GuildMainMenu(openedFromHQ: Boolean = false, player: Player = CurrentPlayer) {
    val owner = LocalGuiyOwner.current
    //TODO koin inject
    val guildViewModel = viewModel { GuildViewModel(player, openedFromHQ, owner, GuildRepository(abyss.db)) }
    guildViewModel.apply {
        nav.withScreen(setOf(player), onEmpty = owner::exit) { screen ->
            Chest(
                screen.title,
                Modifier.height(screen.height),
                onClose = { player.closeInventory() }) {
                when (screen) {
                    is Default -> HomeScreen()
                    is GuildInfo -> GuildInfoScreen()
                    Leave -> GuildLeaveScreen()
                    is GuildList -> GuildLookupListScreen()
                    is GuildLookupMembers -> GuildLookupMembersScreen(screen.guildName)
                    InviteList -> GuildInviteListScreen()
                    is Invite -> GuildInviteScreen(screen.owner)
                    JoinRequestList -> GuildJoinRequestListScreen()
                    is JoinRequest -> GuildJoinRequestScreen(screen.from)
                    Disband -> GuildDisbandScreen()
                    is MemberOptions -> GuildMemberOptionsScreen(screen.member)
                    is MemberList -> GuildMemberListScreen()
                }
            }
        }
    }
}

@Composable
fun BackButton(modifier: Modifier = Modifier, guild: GuildViewModel = viewModel()) {
    Button(onClick = { guild.nav.back() }, modifier = modifier) {
        Text("<red><b>Back")
    }
}

@Composable
fun CloseButton(
    modifier: Modifier = Modifier,
    player: Player = CurrentPlayer,
) {
    Button(onClick = { player.closeInventory() }, modifier = modifier) {
        Text("<red><b>Close")
    }
}

@Composable
fun CurrentGuildButton(
    onClick: () -> Unit,
    player: Player = CurrentPlayer,
) = Button(
    enabled = player.hasGuild(),
    onClick = onClick,
) { enabled ->
    if (enabled) Text(
        "<gold><b>Current Guild Info",
        modifier = Modifier.size(2, 2)
    ) else Text(
        "<gold><b><st>View Guild Information",
        "<red>You are not a member of any guild.",
        modifier = Modifier.size(2, 2)
    )
}

@Composable
fun CreateGuildButton(
    guild: GuildViewModel = viewModel(),
    player: Player = CurrentPlayer,
) = Button(
    enabled = !player.hasGuild(),
    onClick = {
        val guildRenamePaper = TitleItem.of("Guild Name").editItemMeta { isHideTooltip = true }
        if (player.hasGuild()) {
            player.error("You already have a guild.")
            guild.nav.back()
        } else if (!guild.openedFromHQ) {
            player.error("You need to register your guild")
            player.error("with the Guild Master at Orth GuildHQ.")
            player.closeInventory()
        } else guild.nav.open(
            UniversalScreens.Anvil(
                AnvilGUI.Builder()
                    .title(":space_-61::guild_name_menu:")
                    .itemLeft(guildRenamePaper)
                    .itemOutput(TitleItem.transparentItem)
                    .plugin(guiyPlugin)
                    .onClose { guild.nav.back() }
                    .onClick { _, snapshot ->
                        snapshot.player.createGuild(snapshot.text)
                        guild.nav.open(Default(snapshot.player))
                        listOf(ResponseAction.close())
                    }
            ))
    }
) { enabled ->
    if (enabled) Text("<gold><b>Create a Guild", modifier = Modifier.size(2, 2))
    else Text(
        "<gold><i><st>Create a Guild",
        "<red>You have to leave your current",
        "<red>Guild before you can create one.",
        modifier = Modifier.size(2, 2)
    )
}

@Composable
fun GuildInvitesButton(
    guild: GuildViewModel = viewModel(),
    player: Player = CurrentPlayer,
) {
    val guildOwner = player.getGuildOwnerFromInvite().toOfflinePlayer()
    Button(
        enabled = player.hasGuildInvite(guildOwner),
        onClick = { guild.nav.open(InviteList) },
    ) { enabled ->
        /* Icon that notifies player there are new invites */
        if (enabled) Text("<dark_green>Manage Guild Invites")
        /* Custom Icon for "darkerened" out icon indicating no invites */
        else Text("<dark_green><st>Manage Guild Invites")
    }
}

@Composable
fun GuildLookupListButton(
    guild: GuildViewModel = viewModel(),
) = Button(
    enabled = getAllGuilds().isNotEmpty(),
    onClick = { guild.nav.open(GuildList) }
) { enabled ->
    if (enabled) {
        Text("<gold><b>Browse all Guilds", modifier = Modifier.size(2, 2))
    } else Text(
        "<gold><b><st>Browse all Guilds",
        "<yellow>There are currently no Guilds registered.",
        modifier = Modifier.size(2, 2)
    )
}

object DecideMenus {
    fun decideMainMenu(player: Player): String {
        return buildString {
            append(":guild_main_menu:")
            append(":space_-138:")
            if (player.hasGuild()) append(":guild_main_menu_info:")
            else append(":guild_main_menu_create:")
            append(":space_66:")
            if (player.hasGuildInvites()) append(":guild_inbox_unread:")
            else append(":guild_inbox_read:")
        }
    }

    fun decideInfoMenu(isGuildOwner: Boolean): String {
        return buildString {
            append(":guild_info_menu:")
            append(":space_-28:")
            if (isGuildOwner) {
                append(":guild_disband_button:")
                append(":space_-18:")
                append(":guild_level_up_button:")
            } else append(":guild_leave_button:")
        }
    }

    //TODO Implement lists for guilds, making one able to have more than 5(25) members
    fun decideMemberMenu(player: Player, joinType: GuildJoinType): String {
        val menuHeight = minOf(player.getGuildLevel(), 4)
        return buildString {
            append(":guild_member_management_menu_${menuHeight}:")
            append(":space_-171:")
            append(":guild_member_management_jointype_${joinType.name.lowercase()}:")
            append(":space_125:")
            if (player.hasGuildRequests()) append(":guild_inbox_unread:")
            else append(":guild_inbox_read:")
        }
    }
}
