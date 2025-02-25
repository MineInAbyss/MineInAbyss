package com.mineinabyss.features.guilds.menus.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.guilds.menus.BackButton
import com.mineinabyss.features.guilds.menus.GuildScreen
import com.mineinabyss.features.guilds.menus.GuildViewModel
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.inventory.CurrentPlayer
import com.mineinabyss.guiy.inventory.viewModel
import com.mineinabyss.guiy.layout.Column
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.navigation.UniversalScreens
import com.mineinabyss.idofront.items.editItemMeta
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.entity.Player

@Composable
fun GuildInfoScreen(
    player: Player = CurrentPlayer,
) {
    val isOwner = player.isGuildOwner()
    Column(Modifier.at(2, 0)) {
        Row {
            GuildMemberManagement()
            Spacer(width = 1)
            if (isOwner) GuildRenameButton()
            else CurrentGuildInfoButton()
        }
        Spacer(height = 1)
        Row {
            GuildHouseButton()
            Spacer(width = 1)
            GuildRelationshipButton()
        }
    }

    if (isOwner) {
        GuildLevelUpButton(Modifier.at(8, 0))
        GuildDisbandButton(Modifier.at(8, 5))
    } else GuildLeaveButton(Modifier.at(8, 5))

    BackButton(Modifier.at(0, 5))
}

@Composable
fun CurrentGuildInfoButton(
    modifier: Modifier = Modifier,
    guild: GuildViewModel = viewModel(),
) {
    val guild by guild.guildUiState.collectAsState()
    Button(modifier = modifier) {
        Text(
            "<gold><b>Current Guild Info</b>",
            "<yellow><b>Guild Name:</b> <yellow><i>${guild?.name}",
            "<yellow><b>Guild Owner:</b> <yellow><i>${guild?.owner?.name}",
            "<yellow><b>Guild Level:</b> <yellow><i>${guild?.level}",
            "<yellow><b>Guild Members:</b> <yellow><i>${guild?.memberCount}",
            modifier = Modifier.size(2, 2)
        )
    }
}

@Composable
fun GuildMemberManagement(
    modifier: Modifier = Modifier,
    player: Player = CurrentPlayer,
    guild: GuildViewModel = viewModel(),
) {
    Button(
        modifier = modifier,
        onClick = {
            guild.nav.open(GuildScreen.MemberList(guild.guildLevel, player))
        }
    ) {
        Text("<green><b>Guild Member List", modifier = Modifier.size(2, 2))
    }
}

@Composable
fun GuildRenameButton(
    modifier: Modifier = Modifier,
    player: Player = CurrentPlayer,
    guildViewModel: GuildViewModel = viewModel(),
) {
    val guild by guildViewModel.guildUiState.collectAsState()
    val renameItem = TitleItem.of(guild?.name ?: "Guild Name").editItemMeta { isHideTooltip = true }
    Button(
        enabled = player.isCaptainOrAbove(),
        modifier = modifier,
        onClick = {
            if (!player.isCaptainOrAbove()) return@Button
            guildViewModel.nav.open(
                UniversalScreens.Anvil(
                    AnvilGUI.Builder()
                        .title(":space_-61::guild_name_menu:")
                        .itemLeft(renameItem)
                        .itemOutput(TitleItem.transparentItem)
                        .plugin(guiyPlugin)
                        .onClose { guildViewModel.nav.back() }
                        .onClick { _, snapshot ->
                            snapshot.player.changeStoredGuildName(snapshot.text)
                            listOf(AnvilGUI.ResponseAction.close())
                        }
                ))
        }
    ) {
        Text(
            "<gold><b>Change Guild Name",
            "<yellow><b>Guild Name:</b> <yellow><i>${guild?.name}",
            "<yellow><b>Guild Owner:</b> <yellow><i>${guild?.owner?.name}",
            "<yellow><b>Guild Level:</b> <yellow><i>${guild?.level}",
            "<yellow><b>Guild Members:</b> <yellow><i>${guild?.memberCount}",
            "<yellow><b>Guild Balance:</b> <yellow><i>${guild?.balance}",
            modifier = Modifier.size(2, 2)
        )
    }
}

@Composable
fun GuildLevelUpButton(
    modifier: Modifier = Modifier,
    player: Player = CurrentPlayer,
) {
    val guild = player.getGuildName()
    val isMaxLevel = guild?.getGuildLevelUpCost() == null
    Button(
        enabled = player.canLevelUpGuild(),
        onClick = { player.levelUpGuild() },
        modifier = modifier
    ) { enabled ->
        when {
            enabled ->
                Text(
                    "<red><b>Level up Guildrank",
                    "<gold>Next level-up will cost <b>${guild?.getGuildLevelUpCost()} coins</b>."
                )

            isMaxLevel ->
                Text(
                    "<red><b><st>Level up Guildrank",
                    "<dark_red>Your guild has reached the current max-level."
                )

            else ->
                Text(
                    "<red><b><st>Level up Guildrank",
                    "<red>You need <b>${guild?.getGuildLevelUpCost()} coins</b> in your",
                    "<red>guild balance to level up your guildrank."
                )
        }
    }
}

@Composable
fun GuildHouseButton(modifier: Modifier = Modifier) {
    Button(modifier = modifier) {
        Text(
            "<gold><b><st>Guild Housing",
            "<red>This feature is not yet implemented.",
            modifier = Modifier.size(2, 2)
        )
    }
}

@Composable
fun GuildRelationshipButton(modifier: Modifier = Modifier) {
    Button(modifier = modifier) {
        Text(
            "<dark_red><b><st>Guild Wars",
            "<red>This feature is not yet implemented.",
            modifier = Modifier.size(2, 2),
        )
    }
}

@Composable
fun GuildDisbandButton(
    modifier: Modifier = Modifier,
    player: Player = CurrentPlayer,
    guild: GuildViewModel = viewModel(),
) {
    Button(
        modifier = modifier,
        enabled = (player.isGuildOwner()),
        onClick = { guild.nav.open(GuildScreen.Disband) }
    ) { enabled ->
        if (enabled)
            Text("<red><b>Disband Guild")
        else
            Text("<red><b><st>Disband Guild")
    }
}

@Composable
fun GuildLeaveButton(modifier: Modifier, player: Player = CurrentPlayer, guild: GuildViewModel = viewModel()) {
    Button(
        modifier = modifier,
        enabled = player.hasGuild() && !player.isGuildOwner(),
        onClick = {
            guild.nav.open(GuildScreen.Leave)
        }) { enabled ->
        if (enabled) Text("<red><i>Leave Guild")
        else Text("<red><i><st>Leave Guild")
    }
}
