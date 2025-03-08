package com.mineinabyss.features.guilds.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mineinabyss.features.guilds.ui.DecideMenus
import com.mineinabyss.features.guilds.ui.GuildViewModel
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.inventory.CurrentPlayer
import com.mineinabyss.guiy.layout.Column
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.viewmodel.viewModel
import org.bukkit.entity.Player

@Composable
fun HomeScreen(
    guild: GuildViewModel = viewModel(),
    player: Player = CurrentPlayer,
    onNavigateToInfo: () -> Unit,
    onNavigateToInvites: () -> Unit,
    onOpenGuildCreateDialog: () -> Unit,
) = Chest(":space_-8:${DecideMenus.decideMainMenu(player)}", Modifier.height(4)) {
    val guild by guild.currentGuild.collectAsState()
    Row(Modifier.at(2, 1)) {
        if (guild != null) Button(onClick = onNavigateToInfo) {
            Text(
                "<gold><b>Current Guild Info",
                modifier = Modifier.size(2, 2)
            )
        }
        else Button(onClick = { onOpenGuildCreateDialog() }) {
            Text(
                "<gold><b>Create a Guild",
                modifier = Modifier.size(2, 2)
            )
        }

        Spacer(1)
//        GuildLookupListButton()
    }

    Column(Modifier.at(8, 0)) {
//        GuildInvitesButton()
    }

    CloseButton(Modifier.at(0, 3))
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


//@Composable
//fun GuildInvitesButton(
//    guild: GuildViewModel = viewModel(),
//    player: Player = CurrentPlayer,
//) {
//    val guildOwner = player.getGuildOwnerFromInvite().toOfflinePlayer()
//    Button(
//        enabled = player.hasGuildInvite(guildOwner),
//        onClick = { guild.nav.open(InviteList) },
//    ) { enabled ->
//        /* Icon that notifies player there are new invites */
//        if (enabled) Text("<dark_green>Manage Guild Invites")
//        /* Custom Icon for "darkerened" out icon indicating no invites */
//        else Text("<dark_green><st>Manage Guild Invites")
//    }
//}
//
//@Composable
//fun GuildLookupListButton(
//    guild: GuildViewModel = viewModel(),
//) = Button(
//    enabled = getAllGuilds().isNotEmpty(),
//    onClick = { guild.nav.open(GuildList) }
//) { enabled ->
//    if (enabled) {
//        Text("<gold><b>Browse all Guilds", modifier = Modifier.size(2, 2))
//    } else Text(
//        "<gold><b><st>Browse all Guilds",
//        "<yellow>There are currently no Guilds registered.",
//        modifier = Modifier.size(2, 2)
//    )
//}
