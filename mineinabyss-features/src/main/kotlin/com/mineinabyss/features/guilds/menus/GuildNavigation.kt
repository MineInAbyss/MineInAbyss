package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.GuildFeature
import com.mineinabyss.features.guilds.GuildsConfig
import com.mineinabyss.features.guilds.database.GuildJoinType
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.guilds.menus.DecideMenus.decideMainMenu
import com.mineinabyss.features.guilds.menus.GuildScreen.*
import com.mineinabyss.features.guilds.menus.GuildScreen.Invite
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.canvas.GuiyOwner
import com.mineinabyss.guiy.canvas.LocalGuiyOwner
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.layout.Column
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.navigation.*
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.registry.data.dialog.input.DialogInput
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

sealed class GuildScreen() {
    class Default() : GuildScreen()

    class GuildInfo() : GuildScreen()

    data object Leave : GuildScreen()
    data object Disband : GuildScreen()

    data object GuildList : GuildScreen()
    class GuildLookupMembers(val guildName: GuildName) : GuildScreen()

    // Forgot to add to pack so this is fine for now
    data object InviteList : GuildScreen()
    class Invite(val owner: OfflinePlayer) : GuildScreen()

    data object JoinRequestList : GuildScreen()
    class JoinRequest(val from: OfflinePlayer) : GuildScreen()

    class MemberOptions(val member: OfflinePlayer) : GuildScreen()

    class MemberList() : GuildScreen()
}

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
}

@Composable
fun GuildMainMenu(player: Player, openedFromHQ: Boolean = false) {
    val owner = LocalGuiyOwner.current
    val scope = remember { GuildUIScope(player, owner) }
    scope.apply {
        val nav = rememberNavController()
        NavHost<GuildScreen>(nav, startDestination = Default()) {
            composable<Default> {
                HomeScreen(
                    openedFromHQ,
                    onNavigateToInfo = { nav.navigate(GuildInfo()) },
                    onNavigateToGuildList = { nav.navigate(GuildList) },
                    onNavigateToDefault = {
                        nav.reset()
                        nav.navigate(Default())
                    },
                    onNavigateToInviteList = { nav.navigate(InviteList) },
                    onBack = { nav.popBackStack() },
                )
            }
            composable<GuildInfo> {
                GuildInfoScreen(
                    onNavigateToMemberList = { nav.navigate(MemberList()) },
                    onNavigateToDisband = { nav.navigate(Disband) },
                    onNavigateToLeave = { nav.navigate(Leave) },
                )
            }
            composable<Leave> { GuildLeaveScreen() }
            composable<GuildList> {
                GuildLookupListScreen(
                    onNavigateToMemberList = { nav.navigate(MemberList()) },
                    onNavigateToLookupMembers = { nav.navigate(GuildLookupMembers(it)) }
                )
            }
            composable<GuildLookupMembers> { screen -> GuildLookupMembersScreen(screen.guildName) }
            composable<InviteList> {
                GuildInviteListScreen(
                    onNavigateToInviteScreen = { nav.navigate(Invite(it)) },
                    onNavigateToMemberList = { nav.navigate(MemberList()) }
                )
            }
            composable<Invite> { screen ->
                GuildInviteScreen(
                    screen.owner,
                    onBack = { nav.popBackStack() },
                    onNavigateHome = { nav.reset() },
                )
            }
            composable<JoinRequestList> {
                GuildJoinRequestListScreen(
                    onNavigateToJoinRequest = { nav.navigate(JoinRequest(it)) },
                    onBack = { nav.popBackStack() },
                )
            }
            composable<JoinRequest> { screen -> GuildJoinRequestScreen(screen.from, onBack = { nav.popBackStack() }) }
            composable<Disband> { GuildDisbandScreen() }
            composable<MemberOptions> { screen -> GuildMemberOptionsScreen(screen.member, onBack = { nav.popBackStack() }) }
            composable<MemberList> {
                GuildMemberListScreen(
                    onNavigateToMemberOptions = { nav.navigate(MemberOptions(it)) },
                    onNavigateToJoinRequests = { nav.navigate(JoinRequestList) }
                )
            }
        }
    }
}

@Composable
fun GuildUIScope.HomeScreen(
    openedFromHQ: Boolean,
    onNavigateToInfo: () -> Unit,
    onNavigateToGuildList: () -> Unit,
    onNavigateToInviteList: () -> Unit,
    onNavigateToDefault: () -> Unit,
    onBack: () -> Unit,
) = Chest(":space_-8:${decideMainMenu(player)}", Modifier.height(4)) {
    val guildOwner by remember { mutableStateOf(player.isGuildOwner()) }
    Row(Modifier.at(2, 1)) {
        if (player.hasGuild()) CurrentGuildButton(onClick = onNavigateToInfo)
        else CreateGuildButton(
            openedFromHQ = openedFromHQ, onNavigateToDefault, onBack
        )

        Spacer(1)
        GuildLookupListButton(onNavigateToGuildList)
    }

    Column(Modifier.at(8, 0)) {
        GuildInvitesButton(onNavigateToInviteList)
    }

    CloseButton(Modifier.at(0, 3))
}

@Composable
fun GuildUIScope.BackButton(modifier: Modifier = Modifier) {
    val dispatcher = LocalBackGestureDispatcher.current
    Button(onClick = { dispatcher.onBack() }, modifier = modifier) {
        Text("<red><b>Back".miniMsg())
    }
}

@Composable
fun GuildUIScope.CloseButton(modifier: Modifier = Modifier) {
    Button(onClick = { owner.exit() }, modifier = modifier) {
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
fun GuildUIScope.CreateGuildButton(
    openedFromHQ: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToDefault: () -> Unit,
) {
    Button(
        enabled = !player.hasGuild(),
        onClick = {
            val guildRenamePaper = TitleItem.of("Guild Name")
            guildRenamePaper.setData(DataComponentTypes.TOOLTIP_DISPLAY, TitleItem.hideTooltip)
            when {
                player.hasGuild() -> {
                    player.error("You already have a guild.")
                    onNavigateBack()
                }

                !openedFromHQ -> {
                    player.error("You need to register your guild")
                    player.error("with the Guild Master at Orth GuildHQ.")
                    owner.exit()
                }

                else -> {
                    val config = abyss.featureManager.getScope(GuildFeature).get<GuildsConfig>()
                    val maxGuildLength = config.guildNameMaxLength
                    val dialog = GuildDialogs(
                        ":space_-28::guild_search_menu:", "<gold>Create Guild...", listOf(
                            DialogInput.text("guild_dialog", "<gold>Create Guild with name...".miniMsg())
                                .initial("${player.name}'s Guild").width(maxGuildLength * 10)
                                .maxLength(maxGuildLength)
                                .build()
                        )
                    ).createGuildLookDialog {
                        player.createGuild(it)
                        onNavigateToDefault()
                    }

                    player.showDialog(dialog)
                }
            }
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
fun GuildUIScope.GuildInvitesButton(
    onNavigateToInviteList: () -> Unit,
) {
    val guildOwner = player.getGuildOwnerFromInvite().toOfflinePlayer()
    Button(
        enabled = player.hasGuildInvite(guildOwner),
        onClick = onNavigateToInviteList,
    ) { enabled ->
        /* Icon that notifies player there are new invites */
        if (enabled) Text("<dark_green>Manage Guild Invites".miniMsg())
        /* Custom Icon for "darkerened" out icon indicating no invites */
        else Text("<dark_green><st>Manage Guild Invites".miniMsg())
    }
}

@Composable
fun GuildUIScope.GuildLookupListButton(
    onNavigateToGuildList: () -> Unit = {},
) {
    Button(
        enabled = getAllGuilds().isNotEmpty(),
        onClick = onNavigateToGuildList,
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
