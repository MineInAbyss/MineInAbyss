package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.mineinabyss.guilds.menus.GuildScreen.*
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.layout.Column
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.*
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.ui.Navigator
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.mineinabyss.data.GuildRanks
import com.mineinabyss.mineinabyss.extensions.*
import com.okkero.skedule.schedule
import de.erethon.headlib.HeadLib
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.ChatColor.*
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

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

    class TextInput(val anvilGUI: AnvilGUI.Builder) : GuildScreen("", 0)
}

typealias GuildNav = Navigator<GuildScreen>

class GuildUIScope(
    val player: Player,
    val owner: GuiyOwner,
) {
    //TODO cache more than just guild level here
    val guildLevel = player.getGuildLevel() ?: 0
    val nav = GuildNav { Default }
}

@Composable
fun GuiyOwner.GuildMainMenu(player: Player, previousScope: GuildUIScope? = null) {
    val scope = remember { previousScope ?: GuildUIScope(player, this) }
    scope.apply {
        val screen = nav.screen ?: run { exit(); return }
//        Anvil(setOf(player), screen.title, onClose = { nav.back() ?: exit() }) {
//            Row {
//                Item(HeadLib.COBBLESTONE_1.toItemStack())
////                Item(HeadLib.COBBLESTONE_2.toItemStack())
////                Item(HeadLib.COBBLESTONE_3.toItemStack())
//            }
//        }
        if (screen is TextInput) {
            LaunchedEffect(screen) {
                guiyPlugin.schedule {
                    screen.anvilGUI.open(player).inventory
                }
            }
//            Inventory(anvil ?: return)
        } else
            Chest(setOf(player), screen.title, Modifier.height(screen.height), onClose = { nav.back() ?: exit() }) {
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

@Composable
fun GuildUIScope.HomeScreen() {
    val isOwner = player.getGuildRank() == GuildRanks.Owner
    // Big center buttons
    Row(Modifier.at(1, 1)) {
        //TODO decide whether we prefer calling nav.open here or in the button composable
        if (isOwner) GuildOwnerButton(Modifier.clickable { nav.open(Owner) })
        CurrentGuildButton(Modifier.clickable { nav.open(CurrentGuild(guildLevel)) })

        if (!player.hasGuild())
            CreateGuildButton()
    }

    // Small top right
    Column(Modifier.at(8, 0)) {
        GuildInvitesButton()
        LookForGuildButton()
        if (!isOwner)
            CreateGuildButton()
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
    Button(HeadLib.STONE_ARROW_LEFT.toItemStack("Back"), modifier.clickable { nav.back() })
}

@Composable
fun GuildUIScope.Button(item: ItemStack, modifier: Modifier = Modifier) {
    Item(item, modifier.clickable { //TODO clickable should pass player
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
    })
}

@Composable
fun GuildUIScope.CurrentGuildButton(modifier: Modifier = Modifier) {
    Button(
        if (player.hasGuild()) {
            TitleItem.of(
                "$GOLD${BOLD}Current Guild Info:",
                "$YELLOW${BOLD}Guild Name: $YELLOW$ITALIC${player.getGuildName()}",
                "$YELLOW${BOLD}Guild Owner: $YELLOW$ITALIC${player.getGuildOwner().toPlayer()?.name}",
                "$YELLOW${BOLD}Guild Level: $YELLOW$ITALIC${player.getGuildLevel()}",
                "$YELLOW${BOLD}Guild Members: $YELLOW$ITALIC${player.getGuildMemberCount()}"
            )
        } else {
            TitleItem.of(
                "$GOLD$BOLD${STRIKETHROUGH}View Guild Information",
                "${RED}You are not a member of any guild."
            )
        },
        modifier.size(2, 2)
    )
}

@Composable
fun GuildUIScope.GuildOwnerButton(modifier: Modifier = Modifier) {
    Button(
        TitleItem.of("$RED${BOLD}View Owner Information"),
        modifier.size(2, 2)
    )
}

@Composable
fun GuildUIScope.CreateGuildButton(modifier: Modifier = Modifier) {
    Button(
        if (player.hasGuild()) {
            TitleItem.of(
                "$GOLD$ITALIC${STRIKETHROUGH}Create a Guild",
                "${RED}You have to leave your current",
                "${RED}Guild before you can create one."
            )
        } else {
            TitleItem.of("$GOLD${BOLD}Create a Guild")
        },
        modifier.clickable {
            if (!player.hasGuild()) player.nameGuild()
        }
    )
}

@Composable
fun GuildUIScope.LookForGuildButton(modifier: Modifier = Modifier) {
    Button(
        if (player.hasGuild()) {
            TitleItem.of(
                "$GOLD$ITALIC${STRIKETHROUGH}Look for a Guild",
                "${RED}You have to leave your current",
                "${RED}Guild before you can look for one."
            )
        } else {
            TitleItem.of("$GOLD${BOLD}Look for a Guild")
        },
        modifier.clickable {
            if (!player.hasGuild()) {
                nav.open(TextInput(
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
        }
    )
}

@Composable
fun GuildUIScope.GuildInvitesButton(modifier: Modifier = Modifier) {
    val guildOwner = player.getGuildOwnerFromInvite().toPlayer()!!

    /* Icon that notifies player there are new invites */
    if (player.hasGuildInvite(guildOwner))
        Button(TitleItem.of("${DARK_GREEN}Manage Guild Invites"), modifier.clickable {
            nav.open(InviteList)
        })
    /* Custom Icon for "darkerened" out icon indicating no invites */
    else Button(TitleItem.of("$DARK_GREEN${STRIKETHROUGH}Manage Guild Invites"), modifier)
}

fun Player.nameGuild() {
    val guildRenamePaper = TitleItem.of("Guildname")

    AnvilGUI.Builder()
        .title(":guild_naming:")
        .itemLeft(guildRenamePaper)
        //.preventClose()
        .plugin(guiyPlugin)
        .onComplete { player, guildName: String ->
            player.createGuild(guildName)
            AnvilGUI.Response.close()
        }
        .open(player)
}
