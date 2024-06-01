package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.*
import com.mineinabyss.features.guilds.database.GuildJoinType
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.guilds.menus.DecideMenus.decideMainMenu
import com.mineinabyss.features.guilds.menus.DecideMenus.decideMemberMenu
import com.mineinabyss.features.guilds.menus.GuildScreen.*
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.components.canvases.MAX_CHEST_HEIGHT
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.inventory.LocalGuiyOwner
import com.mineinabyss.guiy.layout.Column
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.navigation.Navigator
import com.mineinabyss.guiy.navigation.UniversalScreens
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.wesjd.anvilgui.AnvilGUI
import net.wesjd.anvilgui.AnvilGUI.ResponseAction
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.JoinType

sealed class GuildScreen(var title: String, val height: Int) {
    class Default(player: Player) :
        GuildScreen(
            title = ":space_-8:${decideMainMenu(player)}",
            height = 4
        )

    object GuildInfo : GuildScreen(":space_-8::guild_member_menu:", 6)

    object Leave : GuildScreen(":space_-8::guild_disband_or_leave_menu:", 5)
    object Disband : GuildScreen(":space_-8::guild_disband_or_leave_menu:", 5)
    object Owner : GuildScreen(":space_-8::guild_owner_menu:", 6)

    class GuildList (val pageNumber: Int): GuildScreen(":space_-8::guild_list_menu:", 6)
    class GuildLookupMembers(val guildName: GuildName) :
        GuildScreen(":space_-8:${":guild_lookup_members${minOf(guildName.getGuildLevel(), 3)}"}:", minOf(guildName.getGuildLevel() + 3, MAX_CHEST_HEIGHT))

    // Forgot to add to pack so this is fine for now
    object InviteList : GuildScreen(":space_-8::guild_join_requests_menu:", 5)
    class Invite(val owner: OfflinePlayer) : GuildScreen(":space_-8::handle_guild_invites:", 5)

    object JoinRequestList : GuildScreen(":space_-8::guild_join_requests_menu:", 5)
    class JoinRequest(val from: OfflinePlayer) :
        GuildScreen(":space_-8::handle_guild_join_requests:", 5)

    class MemberOptions(val member: OfflinePlayer) :
        GuildScreen(":space_-8::guild_member_action_menu:", 5)

    class MemberList(val guildLevel: Int, player: Player) :
        GuildScreen(":space_-8:${decideMemberMenu(player, player.getGuildJoinType())}", minOf(guildLevel + 2, MAX_CHEST_HEIGHT))
}

typealias GuildNav = Navigator<GuildScreen>

class GuildUIScope(
    val player: Player,
    val owner: GuiyOwner,
) {
    //TODO cache more than just guild level here
    val guildName get() = player.getGuildName()
    val guildLevel get() = player.getGuildLevel()
    val guildOwner get() = player.getGuildOwner()?.toOfflinePlayer()
    val memberCount get() = player.getGuildMemberCount()
    val guildBalance get() = player.getGuildBalance()
    val nav = GuildNav { Default(player) }
}

@Composable
fun GuildMainMenu(player: Player, openedFromHQ: Boolean = false) {
    val owner = LocalGuiyOwner.current
    val scope = remember { GuildUIScope(player, owner) }
    scope.apply {
        nav.withScreen(setOf(player), onEmpty = owner::exit) { screen ->
            Chest(
                setOf(player),
                screen.title,
                Modifier.height(screen.height),
                onClose = { player.closeInventory() }) {
                when (screen) {
                    is Default -> HomeScreen(openedFromHQ)
                    GuildInfo -> GuildInfoScreen()
                    Owner -> GuildOwnerScreen()
                    Leave -> GuildLeaveScreen()
                    is GuildList -> GuildLookupListScreen(screen.pageNumber)
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
fun GuildUIScope.HomeScreen(openedFromHQ: Boolean) {
    val guildOwner by remember { mutableStateOf(player.isGuildOwner()) }
    val screen = if (guildOwner) Owner else GuildInfo
    Row(Modifier.at(2, 1)) {
        if (player.hasGuild()) CurrentGuildButton(onClick = { nav.open(screen) })
        else CreateGuildButton(openedFromHQ = openedFromHQ)

        Spacer(1)
        GuildLookupListButton()
    }

    Column(Modifier.at(8, 0)) {
        GuildInvitesButton()
    }

    CloseButton(Modifier.at(0, 3))
}

@Composable
fun GuildUIScope.BackButton(modifier: Modifier = Modifier) {
    Button(onClick = { nav.back() }, modifier = modifier) {
        Text("<red><b>Back".miniMsg())
    }
}

@Composable
fun GuildUIScope.CloseButton(modifier: Modifier = Modifier) {
    Button(onClick = { player.closeInventory() }, modifier = modifier) {
        Text("<red><b>Close".miniMsg())
    }
}

@Composable
fun GuildUIScope.CurrentGuildButton(onClick: () -> Unit) {
    Button(
        enabled = player.hasGuild(),
        onClick = onClick,
    ) { enabled ->
        if (enabled) Text(
            "<gold><b>Current Guild Info".miniMsg(),
            modifier = Modifier.size(2, 2)
        ) else Text(
            "<gold><b><st>View Guild Information".miniMsg(),
            "<red>You are not a member of any guild.".miniMsg(),
            modifier = Modifier.size(2, 2)
        )
    }
}

@Composable
fun GuildUIScope.CreateGuildButton(openedFromHQ: Boolean) {
    Button(
        enabled = !player.hasGuild(),
        onClick = {
            val guildRenamePaper = TitleItem.of("Guild Name")
            if (player.hasGuild()) {
                player.error("You already have a guild.")
                nav.back()
            } else if (!openedFromHQ) {
                player.error("You need to register your guild")
                player.error("with the Guild Master at Orth GuildHQ.")
                player.closeInventory()
            } else nav.open(UniversalScreens.Anvil(
                AnvilGUI.Builder()
                    .title(":space_-61::guild_name_menu:")
                    .itemLeft(guildRenamePaper)
                    .plugin(guiyPlugin)
                    .onClose { nav.back() }
                    .onClick { _, snapshot ->
                        snapshot.player.createGuild(snapshot.text)
                        nav.open(Default(snapshot.player))
                        listOf(ResponseAction.close())
                    }
            ))
        }
    ) { enabled ->
        if (enabled) Text("<gold><b>Create a Guild".miniMsg(), modifier = Modifier.size(2, 2))
        else Text(
            "<gold><i><st>Create a Guild".miniMsg(),
            "<red>You have to leave your current".miniMsg(),
            "<red>Guild before you can create one.".miniMsg(),
            modifier = Modifier.size(2, 2)
        )
    }
}

@Composable
fun GuildUIScope.GuildInvitesButton() {
    val guildOwner = player.getGuildOwnerFromInvite().toOfflinePlayer()
    Button(
        enabled = player.hasGuildInvite(guildOwner),
        onClick = { nav.open(InviteList) },
    ) { enabled ->
        /* Icon that notifies player there are new invites */
        if (enabled) Text("<dark_green>Manage Guild Invites".miniMsg())
        /* Custom Icon for "darkerened" out icon indicating no invites */
        else Text("<dark_green><st>Manage Guild Invites".miniMsg())
    }
}

@Composable
fun GuildUIScope.GuildLookupListButton() {
    Button(
        enabled = getAllGuilds().isNotEmpty(),
        onClick = { nav.open(GuildList(0)) }
    ) { enabled ->
        if (enabled) {
            Text("<gold><b>Browse all Guilds".miniMsg(), modifier = Modifier.size(2, 2))
        } else Text(
            "<gold><b><st>Browse all Guilds".miniMsg(),
            "<yellow>There are currently no Guilds registered.".miniMsg(),
            modifier = Modifier.size(2, 2)
        )
    }
}

object DecideMenus {
    private const val hasGuildAndInvites = ":guild_main_menu_has_guild_and_has_invites:"
    private const val hasGuildAndNoInvites = ":guild_main_menu_has_guild_and_no_invites:"
    private const val noGuildAndInvites = ":guild_main_menu_no_guild_and_has_invites:"
    private const val noGuildAndNoInvites = ":guild_main_menu_no_guild_and_no_invites:"

    fun decideMainMenu(player: Player): String {
        return when {
            (player.hasGuild() && player.hasGuildInvites()) -> hasGuildAndInvites
            (player.hasGuild() && !player.hasGuildInvites()) -> hasGuildAndNoInvites
            (!player.hasGuild() && player.hasGuildInvites()) -> noGuildAndInvites
            else -> noGuildAndNoInvites
        }
    }

    //TODO Implement lists for guilds, making one able to have more than 5(25) members
    fun decideMemberMenu(player: Player, joinType: GuildJoinType): String {
        val menuHeight = minOf(player.getGuildLevel(), 4)
        return buildString {
            append(":guild_member_management_menu_${menuHeight}:")
            append(":space_-172:")
            append(":guild_member_management_jointype_${joinType.name.lowercase()}:")
            if (!player.hasGuildRequests()) append(":space_135::guild_member_management_notification:")
        }
    }
}
