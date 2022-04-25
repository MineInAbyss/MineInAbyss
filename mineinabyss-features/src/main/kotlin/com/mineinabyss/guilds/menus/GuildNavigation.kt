package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mineinabyss.guilds.GuildFeature
import com.mineinabyss.guilds.extensions.createGuild
import com.mineinabyss.guilds.extensions.getAllGuilds
import com.mineinabyss.guilds.menus.DecideMenus.decideMainMenu
import com.mineinabyss.guilds.menus.DecideMenus.decideMemberMenu
import com.mineinabyss.guilds.menus.GuildScreen.*
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.layout.Column
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.NoToolTip
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.ui.Navigator
import com.mineinabyss.helpers.ui.UniversalScreens
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.mineinabyss.extensions.*
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.ChatColor.*
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

sealed class GuildScreen(var title: String, val height: Int) {
    class Default(player: Player) :
        GuildScreen(
            title = "${Space.of(-14)}$WHITE${decideMainMenu(player)}",
            height = 4
        )

    object GuildInfo : GuildScreen("${Space.of(-12)}$WHITE:guild_member_menu:", 6)

    class CurrentGuild(val guildLevel: Int) :
        GuildScreen("${Space.of(-12)}${Space.of(1)}$WHITE:current_guild_menu_${guildLevel}:", guildLevel + 3)

    object Leave : GuildScreen("${Space.of(-12)}$WHITE:guild_disband_or_leave_menu:", 5)
    object Disband : GuildScreen("${Space.of(-12)}$WHITE:guild_disband_or_leave_menu:", 5)
    object Owner : GuildScreen("${Space.of(-12)}$WHITE:guild_owner_menu:", 6)

    object GuildList : GuildScreen(title = "${Space.of(-12)}$WHITE:guild_list_menu:", 6)
    class GuildLookupMembers(val guildName: String) :
        GuildScreen("${Space.of(-12)}$WHITE${":guild_lookup_members${guildName.getGuildLevel()}"}:", guildName.getGuildLevel() + 3)

    // Forgot to add to pack so this is fine for now
    object InviteList : GuildScreen(title = "${Space.of(-12)}$WHITE:guild_join_requests_menu:", 5)
    class Invite(val owner: OfflinePlayer) : GuildScreen(title = "${Space.of(-12)}$WHITE:handle_guild_invites:", 5)

    object JoinRequestList : GuildScreen(title = "${Space.of(-12)}$WHITE:guild_join_requests_menu:", 5)
    class JoinRequest(val from: OfflinePlayer) :
        GuildScreen("${Space.of(-12)}$WHITE:handle_guild_join_requests:", 5)

    class MemberOptions(val member: OfflinePlayer) :
        GuildScreen("${Space.of(-12)}$WHITE:guild_member_action_menu:", 5)

    class MemberList(val guildLevel: Int, player: Player) :
        GuildScreen("${Space.of(-12)}$WHITE${decideMemberMenu(player)}", guildLevel + 2)
}

typealias GuildNav = Navigator<GuildScreen>

class GuildUIScope(
    val player: Player,
    val owner: GuiyOwner,
    val feature: GuildFeature
) {
    //TODO cache more than just guild level here
    val guildLevel = player.getGuildLevel() ?: 0
    val nav = GuildNav { Default(player) }
}

@Composable
fun GuiyOwner.GuildMainMenu(player: Player, feature: GuildFeature, openedFromHQ: Boolean = false) {
    val scope = remember { GuildUIScope(player, this, feature) }
    scope.apply {
        nav.withScreen(setOf(player), onEmpty = ::exit) { screen ->
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
                    is CurrentGuild -> CurrentGuildScreen()
                    GuildList -> GuildLookupListScreen()
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
    Row(Modifier.at(2, 1)) {
        if (player.hasGuild() && player.isGuildOwner()) CurrentGuildButton(onClick = { nav.open(Owner) })
        else if (player.hasGuild() && !player.isGuildOwner()) CurrentGuildButton(onClick = { nav.open(GuildInfo) })
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
        Text("$RED${BOLD}Back")
    }
}

@Composable
fun GuildUIScope.CloseButton(modifier: Modifier = Modifier) {
    Button(onClick = { player.closeInventory() }, modifier = modifier) {
        Text("$RED${BOLD}Close")
    }
}

@Composable
fun GuildUIScope.CurrentGuildButton(onClick: () -> Unit) {
    Button(
        enabled = player.hasGuild(),
        onClick = onClick,
    ) { enabled ->
        if (enabled) Text(
            "$GOLD${BOLD}Current Guild Info",
            modifier = Modifier.size(2, 2)
        ) else Text(
            "$GOLD$BOLD${STRIKETHROUGH}View Guild Information",
            "${RED}You are not a member of any guild.",
            modifier = Modifier.size(2, 2)
        )
    }
}

@Composable
fun GuildUIScope.CreateGuildButton(openedFromHQ: Boolean) {
    Button(
        enabled = !player.hasGuild(),
        onClick = {
            val guildRenamePaper = TitleItem.of("Guild Name").NoToolTip()
            if (player.hasGuild()) {
                player.error("You already have a guild.")
                nav.back()
                return@Button
            }
            if (!openedFromHQ) {
                player.error("You need to register your guild")
                player.error("with the Guild Master at Orth GuildHQ.")
                player.closeInventory()
                return@Button
            }
            nav.open(UniversalScreens.Anvil(
                AnvilGUI.Builder()
                    .title("${Space.of(-64)}:guild_name_menu:")
                    .itemLeft(guildRenamePaper)
                    .plugin(guiyPlugin)
                    .onClose { nav.back() }
                    .onComplete { player, guildName: String ->
                        player.createGuild(guildName, feature)
                        nav.open(Default(player))
                        AnvilGUI.Response.close()
                    }
            ))
        }
    ) { enabled ->
        if (enabled) Text("$GOLD${BOLD}Create a Guild", modifier = Modifier.size(2, 2))
        else Text(
            "$GOLD$ITALIC${STRIKETHROUGH}Create a Guild",
            "${RED}You have to leave your current",
            "${RED}Guild before you can create one.",
            modifier = Modifier.size(2, 2)
        )
    }
}

@Composable
fun GuildUIScope.GuildInvitesButton(modifier: Modifier = Modifier) {
    val guildOwner = player.getGuildOwnerFromInvite().toPlayer() ?: return
    Button(
        enabled = player.hasGuildInvite(guildOwner),
        onClick = { nav.open(InviteList) },
    ) { enabled ->
        /* Icon that notifies player there are new invites */
        if (enabled) {
            Text("${DARK_GREEN}Manage Guild Invites")
        }
        /* Custom Icon for "darkerened" out icon indicating no invites */
        else {
            Text("$DARK_GREEN${STRIKETHROUGH}Manage Guild Invites")
        }
    }
}

@Composable
fun GuildUIScope.GuildLookupListButton(modifier: Modifier = Modifier) {
    Button(
        enabled = getAllGuilds().isNotEmpty(),
        onClick = { nav.open(GuildList) }
    ) { enabled ->
        if (enabled) {
            Text("$GOLD${BOLD}Browse all Guilds", modifier = Modifier.size(2, 2))
        } else Text(
            "$GOLD${BOLD}${STRIKETHROUGH}Browse all Guilds",
            "${YELLOW}There are currently no Guilds registered.",
            modifier = Modifier.size(2, 2)
        )
    }
}

object DecideMenus {
    val hasGuildAndInvites = ":guild_main_menu_has_guild_and_has_invites:"
    val hasGuildAndNoInvites = ":guild_main_menu_has_guild_and_no_invites:"
    val noGuildAndInvites = ":guild_main_menu_no_guild_and_has_invites:"
    val noGuildAndNoInvites = ":guild_main_menu_no_guild_and_no_invites:"

    fun decideMainMenu(player: Player): String {
        return when {
            (player.hasGuild() && player.hasGuildInvites()) -> hasGuildAndInvites
            (player.hasGuild() && !player.hasGuildInvites()) -> hasGuildAndNoInvites
            (!player.hasGuild() && player.hasGuildInvites()) -> noGuildAndInvites
            else -> noGuildAndNoInvites
        }
    }

    fun decideMemberMenu(player: Player): String {
        return if (player.hasGuildRequests()) ":guild_member_management_menu_${player.getGuildLevel()}_has_request:"
        else ":guild_member_management_menu_${player.getGuildLevel()}_no_request:"
    }
}
