package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mineinabyss.guilds.GuildFeature
import com.mineinabyss.guilds.database.GuildRanks
import com.mineinabyss.guilds.extensions.createGuild
import com.mineinabyss.guilds.menus.GuildScreen.*
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.layout.Column
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.ui.Navigator
import com.mineinabyss.helpers.ui.UniversalScreens
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.mineinabyss.extensions.*
import de.erethon.headlib.HeadLib
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.ChatColor.*
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

sealed class GuildScreen(val title: String, val height: Int) {
    object Default : GuildScreen("${Space.of(-18)}$WHITE:main_guild_menu:", 4)

    class CurrentGuild(val guildLevel: Int) : GuildScreen("${Space.of(-18)}$WHITE:current_guild_menu:", guildLevel + 2)
    object Disband : GuildScreen("${Space.of(-18)}$WHITE:disband_guild_menu:", 4)
    object Owner : GuildScreen("${Space.of(-18)}$WHITE:guild_owner_menu:", 6)
    object InviteList : GuildScreen("${Space.of(-18)}$WHITE:guild_invites_menu:", 4)
    class Invite(val owner: OfflinePlayer) : GuildScreen("${Space.of(-18)}$WHITE:handle_guild_invites:", 5)

    object JoinRequestList : GuildScreen("${Space.of(-18)}$WHITE:handle_guild_join_requests:", 5)
    class JoinRequest(val from: OfflinePlayer) : GuildScreen("${Space.of(-18)}$WHITE:handle_guild_join_requests:", 5)

    class MemberOptions(val member: OfflinePlayer) : GuildScreen("${Space.of(-18)}$WHITE:guild_member_action_menu:", 5)
    class MemberList(val guildLevel: Int) :
        GuildScreen("${Space.of(-18)}$WHITE:guild_member_management_menu:", guildLevel + 2)
}

typealias GuildNav = Navigator<GuildScreen>

class GuildUIScope(
    val player: Player,
    val owner: GuiyOwner,
    val feature: GuildFeature
) {
    //TODO cache more than just guild level here
    val guildLevel = player.getGuildLevel() ?: 0
    val nav = GuildNav { Default }
}

@Composable
fun GuiyOwner.GuildMainMenu(player: Player, feature: GuildFeature) {
    val scope = remember { GuildUIScope(player, this, feature) }
    scope.apply {
        nav.withScreen(setOf(player), onEmpty = ::exit) { screen ->
            Chest(
                setOf(player),
                screen.title,
                Modifier.height(screen.height),
                onClose = { nav.back() ?: exit() }) {
                when (screen) {
                    Default -> HomeScreen()
                    Owner -> GuildOwnerScreen()
                    is CurrentGuild -> CurrentGuildScreen()
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
fun GuildUIScope.HomeScreen() {
    val isOwner = player.getGuildRank() == GuildRanks.Owner
    // Big center buttons
    Row(Modifier.at(1, 1)) {
        //TODO decide whether we prefer calling nav.open here or in the button composable
        if (isOwner) GuildOwnerButton(onClick = { nav.open(Owner) })
        CurrentGuildButton(onClick = { nav.open(CurrentGuild(guildLevel)) })

        if (!player.hasGuild())
            CreateGuildButton()
    }

    // Small top right
    Column(Modifier.at(8, 0)) {
        GuildInvitesButton()
        LookForGuildButton()
    }

    // Bottom right
    Row(Modifier.at(8, 3)) {
        if (!isOwner) LeaveGuildButton(player)
        //else disband?
    }

    BackButton(Modifier.at(0, 3))
}

@Composable
fun GuildUIScope.BackButton(modifier: Modifier = Modifier) {
    Button(onClick = { nav.back() }, modifier = modifier) {
        Item(HeadLib.STONE_ARROW_LEFT.toItemStack("Back"))
    }
}

@Composable
fun GuildUIScope.CurrentGuildButton(onClick: () -> Unit) {
    Button(
        enabled = player.hasGuild(),
        onClick = onClick,
    ) { enabled ->
        if (enabled) Text(
            "$GOLD${BOLD}Current Guild Info:",
            "$YELLOW${BOLD}Guild Name: $YELLOW$ITALIC${player.getGuildName()}",
            "$YELLOW${BOLD}Guild Owner: $YELLOW$ITALIC${player.getGuildOwner().toPlayer()?.name}",
            "$YELLOW${BOLD}Guild Level: $YELLOW$ITALIC${player.getGuildLevel()}",
            "$YELLOW${BOLD}Guild Members: $YELLOW$ITALIC${player.getGuildMemberCount()}",
            modifier = Modifier.size(2, 2)
        ) else Text(
            "$GOLD$BOLD${STRIKETHROUGH}View Guild Information",
            "${RED}You are not a member of any guild.",
            modifier = Modifier.size(2, 2)
        )
    }
}

@Composable
fun GuildUIScope.GuildOwnerButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(
            "$RED${BOLD}View Owner Information",
            modifier = Modifier.size(2, 2)
        )
    }
}

@Composable
fun GuildUIScope.CreateGuildButton() {
    Button(
        enabled = !player.hasGuild(),
        onClick = {
            val guildRenamePaper = TitleItem.of("Guildname")
            nav.open(UniversalScreens.Anvil(
                AnvilGUI.Builder()
                    .title(":guild_naming:")
                    .itemLeft(guildRenamePaper)
                    .plugin(guiyPlugin)
                    .onClose { nav.back() }
                    .onComplete { player, guildName: String ->
                        player.createGuild(guildName, feature)
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
fun GuildUIScope.LookForGuildButton() {
    Button(
        enabled = !player.hasGuild(),
        onClick = {
            nav.open(UniversalScreens.Anvil(
                AnvilGUI.Builder()
                    .title(":guild_request:")
                    .itemLeft(TitleItem.of("Guildname"))
                    //.preventClose()
                    .plugin(guiyPlugin)
                    .onClose { nav.back() }
                    .onComplete { player, guildName: String ->
                        player.lookForGuild(guildName)
                        AnvilGUI.Response.close()
                    }
            ))
        }
    ) { enabled ->
        if (enabled) Text("$GOLD${BOLD}Look for a Guild")
        else Text(
            "$GOLD$ITALIC${STRIKETHROUGH}Look for a Guild",
            "${RED}You have to leave your current",
            "${RED}Guild before you can look for one."
        )
    }
}

@Composable
fun GuildUIScope.GuildInvitesButton(modifier: Modifier = Modifier) {
    val guildOwner = player.getGuildOwnerFromInvite().toPlayer()!!
    Button(
        enabled = player.hasGuildInvite(guildOwner),
        onClick = { nav.open(InviteList) },
    ) { enabled ->
        /* Icon that notifies player there are new invites */
        if (enabled) Text("${DARK_GREEN}Manage Guild Invites")
        /* Custom Icon for "darkerened" out icon indicating no invites */
        else Text("$DARK_GREEN${STRIKETHROUGH}Manage Guild Invites")
    }
}
