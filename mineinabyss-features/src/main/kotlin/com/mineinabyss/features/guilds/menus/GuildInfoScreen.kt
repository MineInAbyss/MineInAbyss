package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.guilds.menus.DecideMenus.decideInfoMenu
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.registry.data.dialog.input.DialogInput
import me.dvyy.compose.mini.layout.jetpack.Column
import me.dvyy.compose.mini.layout.jetpack.Row
import me.dvyy.compose.mini.layout.modifiers.height
import me.dvyy.compose.mini.layout.modifiers.offset
import me.dvyy.compose.mini.layout.modifiers.size
import me.dvyy.compose.mini.layout.modifiers.width
import me.dvyy.compose.mini.modifier.Modifier
import org.bukkit.entity.Player

@Composable
fun GuildUIScope.GuildInfoScreen(
    onNavigateToMemberList: () -> Unit,
    onNavigateToDisband: () -> Unit,
    onNavigateToLeave: () -> Unit,
) = Chest(":space_-8:${decideInfoMenu(player.isGuildOwner())}", Modifier.height(6.dp)) {
    val isOwner = player.isGuildOwner()
    Column(Modifier.offset(2.dp, 0.dp)) {
        Row {
            GuildMemberManagement(onClick = onNavigateToMemberList)
            Spacer(Modifier.height(1.dp))
            if (isOwner) GuildRenameButton()
            else CurrentGuildInfoButton()
        }
        Spacer(Modifier.height(1.dp))
        Row {
            GuildHouseButton()
            Spacer(Modifier.width(1.dp))
            GuildRelationshipButton()
        }
    }

    if (isOwner) {
        GuildLevelUpButton(Modifier.offset(8.dp, 0.dp))
        GuildDisbandButton(Modifier.offset(8.dp, 5.dp), onNavigateToDisband)
    } else GuildLeaveButton(player, Modifier.offset(8.dp, 5.dp), onNavigateToLeave)

    BackButton(Modifier.offset(0.dp, 5.dp))
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
            modifier = Modifier.size(2.dp, 2.dp)
        )
    }
}

@Composable
fun GuildMemberManagement(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        modifier = modifier,
        onClick = onClick
    ) {
        Text("<green><b>Guild Member List".miniMsg(), modifier = Modifier.size(2.dp, 2.dp))
    }
}

@Composable
fun GuildUIScope.GuildRenameButton(modifier: Modifier = Modifier) {
    Button(
        enabled = player.isCaptainOrAbove(),
        modifier = modifier,
        onClick = {
            if (!player.isCaptainOrAbove()) return@Button

            val maxGuildLength = abyss.guilds.config.guildNameMaxLength
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
            modifier = Modifier.size(2.dp, 2.dp)
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
            "<gold><b><st>Guild Housing".miniMsg(), modifier = Modifier.size(2.dp, 2.dp),
            lore = arrayOf("<red>This feature is not yet implemented.".miniMsg())
        )
    }
}

@Composable
fun GuildRelationshipButton(modifier: Modifier = Modifier) {
    Button(modifier = modifier) {
        Text(
            "<dark_red><b><st>Guild Wars".miniMsg(), modifier = Modifier.size(2.dp, 2.dp),
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
