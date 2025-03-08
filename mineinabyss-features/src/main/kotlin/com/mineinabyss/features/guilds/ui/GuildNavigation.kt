package com.mineinabyss.features.guilds.ui

import androidx.compose.runtime.*
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.data.GuildJoinRequestsRepository
import com.mineinabyss.features.guilds.data.GuildMessagesRepository
import com.mineinabyss.features.guilds.data.GuildRepository
import com.mineinabyss.features.guilds.ui.GuildScreen.*
import com.mineinabyss.features.guilds.ui.screens.*
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.canvases.Anvil
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.inventory.CurrentPlayer
import com.mineinabyss.guiy.inventory.LocalGuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.navigation.LocalBackGestureDispatcher
import com.mineinabyss.guiy.navigation.NavHost
import com.mineinabyss.guiy.navigation.composable
import com.mineinabyss.guiy.navigation.rememberNavController
import com.mineinabyss.guiy.viewmodel.viewModel
import org.bukkit.entity.Player

@Composable
fun GuildMainMenu(openedFromHQ: Boolean = false, player: Player = CurrentPlayer) {
    val owner = LocalGuiyOwner.current
    //TODO koin inject
    val guildViewModel = viewModel {
        GuildViewModel(
            player, openedFromHQ, GuildRepository(abyss.db),
            GuildMessagesRepository(abyss.db),
            GuildJoinRequestsRepository(abyss.db),
        )
    }
    val nav = rememberNavController()
    NavHost(nav, startDestination = Default) {
        composable<Default> {
            HomeScreen(
                onNavigateToInfo = { nav.navigate(GuildInfo) },
                onNavigateToInvites = { nav.navigate(InviteList) },
                onOpenGuildCreateDialog = { nav.navigate(CreateGuild) }
            )
        }
        composable<GuildInfo> {
            GuildInfoScreen(
                onNavigateToRename = { nav.navigate(RenameGuild) },
                onNavigateToMembersList = { nav.navigate(MemberList) },
                onNavigateToDisband = {},
                onNavigateToLeave = {}
            )
        }
        composable<Leave> { GuildLeaveScreen(onLeave = { nav.popBackStack() }) }
        composable<GuildList> { GuildLookupListScreen() }
        composable<GuildLookupMembers> { GuildLookupMembersScreen() }
        composable<InviteList> { GuildInviteListScreen() }
        composable<InviteScreen> { GuildInviteScreen() }
        composable<JoinRequestList> { GuildJoinRequestListScreen() }
        composable<JoinRequest> { GuildJoinRequestScreen(screen.from) }
        composable<Disband> { GuildDisbandScreen() }
        composable<MemberOptions> { GuildMemberOptionsScreen() }
        composable<MemberList> { GuildMemberListScreen() }

        composable<CreateGuild> {
            var input by remember { mutableStateOf("") }
            Anvil(
                ":space_-61::guild_name_menu:",
                onTextChanged = { input = it },
                output = {
                    //TODO validate
                    Text("Create: $input")
                },
                onSubmit = {
                    guildViewModel.createGuild(input)
                    nav.popBackStack()
                }
            )
        }
    }
}

@Composable
fun BackButton(modifier: Modifier = Modifier, content: @Composable () -> Unit = { Text("<red><b>Back") }) {
    val backHandler = LocalBackGestureDispatcher.current ?: return
    Button(onClick = { backHandler.onBack() }, modifier = modifier) {
        content()
    }
}
