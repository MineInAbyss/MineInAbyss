package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.guilds.menus.DecideMenus.decideInfoMenu
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.di.Features
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.layout.Column
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.registry.data.dialog.input.DialogInput
import org.bukkit.entity.Player

@Composable
fun GuildUIScope.GuildInfoScreen(
    onNavigateToMemberList: () -> Unit,
    onNavigateToDisband: () -> Unit,
    onNavigateToLeave: () -> Unit,
) = Chest(":space_-8:${decideInfoMenu(player.isGuildOwner())}", Modifier.height(6)) {
    val isOwner = player.isGuildOwner()
    Column(Modifier.at(2, 0)) {
        Row {
            GuildMemberManagement(onClick = onNavigateToMemberList)
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
        GuildDisbandButton(Modifier.at(8, 5), onNavigateToDisband)
    } else GuildLeaveButton(player, Modifier.at(8, 5), onNavigateToLeave)

    BackButton(Modifier.at(0, 5))
}

@Composable
fun GuildUIScope.CurrentGuildInfoButton(modifier: Modifier = Modifier) {
    Button(modifier = modifier) {
        Text(
            "<gold><b>Current Guild Info</b>".miniMsg(),
            "<yellow><b>Guild Name:</b> <yellow><i>${guildName}".miniMsg(),
            "<yellow><b>Guild Owner:</b> <yellow><i>${guildOwner?.name}".miniMsg(),
            "<yellow><b>Guild Level:</b> <yellow><i>${guildLevel}".miniMsg(),
            "<yellow><b>Guild Members:</b> <yellow><i>${memberCount}".miniMsg(),
            modifier = Modifier.size(2, 2)
        )
    }
}

@Composable
fun GuildMemberManagement(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        modifier = modifier,
        onClick = onClick
    ) {
        Text("<green><b>Guild Member List".miniMsg(), modifier = Modifier.size(2, 2))
    }
}

@Composable
fun GuildUIScope.GuildRenameButton(modifier: Modifier = Modifier) {
    Button(
        enabled = player.isCaptainOrAbove(),
        modifier = modifier,
        onClick = {
            if (!player.isCaptainOrAbove()) return@Button

            val maxGuildLength = Features.guilds.config.guildNameMaxLength
            val dialog = GuildDialogs(
                ":space_-28::guild_name_menu:", "<gold>Rename Guild!", listOf(
                    DialogInput.text("guild_dialog", "<gold>Rename Guild...".miniMsg())
                        .initial(guildName ?: "").width(maxGuildLength * 6)
                        .maxLength(maxGuildLength)
                        .build()
                )
            ).createGuildLookDialog { player.changeStoredGuildName(it) }

            player.showDialog(dialog)
        }
    ) {
        Text(
            "<gold><b>Change Guild Name".miniMsg(),
            "<yellow><b>Guild Name:</b> <yellow><i>${guildName}".miniMsg(),
            "<yellow><b>Guild Owner:</b> <yellow><i>${player.name}".miniMsg(),
            "<yellow><b>Guild Level:</b> <yellow><i>${guildLevel}".miniMsg(),
            "<yellow><b>Guild Members:</b> <yellow><i>${memberCount}".miniMsg(),
            "<yellow><b>Guild Balance:</b> <yellow><i>${guildBalance}".miniMsg(),
            modifier = Modifier.size(2, 2)
        )
    }
}

@Composable
fun GuildUIScope.GuildLevelUpButton(modifier: Modifier = Modifier) {
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
                    "<red><b>Level up Guildrank".miniMsg(),
                    "<gold>Next level-up will cost <b>${guild?.getGuildLevelUpCost()} coins</b>.".miniMsg()
                )

            isMaxLevel ->
                Text(
                    "<red><b><st>Level up Guildrank".miniMsg(),
                    "<dark_red>Your guild has reached the current max-level.".miniMsg()
                )

            else ->
                Text(
                    "<red><b><st>Level up Guildrank".miniMsg(),
                    "<red>You need <b>${guild.getGuildLevelUpCost()} coins</b> in your".miniMsg(),
                    "<red>guild balance to level up your guildrank.".miniMsg()
                )
        }
    }
}

@Composable
fun GuildHouseButton(modifier: Modifier = Modifier) {
    Button(modifier = modifier) {
        Text(
            "<gold><b><st>Guild Housing".miniMsg(), modifier = Modifier.size(2, 2),
            lore = arrayOf("<red>This feature is not yet implemented.".miniMsg())
        )
    }
}

@Composable
fun GuildRelationshipButton(modifier: Modifier = Modifier) {
    Button(modifier = modifier) {
        Text(
            "<dark_red><b><st>Guild Wars".miniMsg(), modifier = Modifier.size(2, 2),
            lore = arrayOf("<red>This feature is not yet implemented.".miniMsg())
        )
    }
}

@Composable
fun GuildUIScope.GuildDisbandButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        modifier = modifier,
        enabled = (player.isGuildOwner()),
        onClick = onClick
    ) { enabled ->
        if (enabled)
            Text("<red><b>Disband Guild".miniMsg())
        else
            Text("<red><b><st>Disband Guild".miniMsg())


    }
}

@Composable
fun GuildUIScope.GuildLeaveButton(player: Player, modifier: Modifier, onClick: () -> Unit) {
    Button(
        modifier = modifier,
        enabled = player.hasGuild() && !player.isGuildOwner(),
        onClick = onClick
    ) { enabled ->
        if (enabled)
            Text("<red><i>Leave Guild".miniMsg())
        else
            Text("<red><i><st>Leave Guild".miniMsg())

    }
}
