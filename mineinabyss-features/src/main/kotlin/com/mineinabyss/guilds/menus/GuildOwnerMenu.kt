package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guilds.extensions.*
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.layout.Column
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.navigation.UniversalScreens
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.messaging.miniMsg
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.entity.Player

@Composable
fun GuildUIScope.GuildOwnerScreen() {
    Column(Modifier.at(2, 0)) {
        Row {
            GuildMemberManagement()
            Spacer(width = 1)
            GuildRenameButton()
        }
        Spacer(height = 1)
        Row {
            GuildHouseButton()
            Spacer(width = 1)
            GuildRelationshipButton()
        }
    }

    if (player.isGuildOwner()) GuildDisbandButton(Modifier.at(8, 5))
    else GuildLeaveButton(player, Modifier.at(8, 5))

    BackButton(Modifier.at(0, 5))
    GuildLevelUpButton(Modifier.at(8, 0))
}

@Composable
fun GuildUIScope.GuildMemberManagement(modifier: Modifier = Modifier) {
    Button(
        modifier = modifier,
        onClick = {
            nav.open(GuildScreen.MemberList(guildLevel, player))
        }
    ) {
        Text("<green><b>Guild Member List".miniMsg(), modifier = Modifier.size(2, 2))
    }
}

@Composable
fun GuildUIScope.GuildRenameButton(modifier: Modifier = Modifier) {
    val renameItem = TitleItem.of(player.getGuildName())
    Button(
        enabled = player.isAboveCaptain(),
        modifier = modifier,
        onClick = {
            if (!player.isAboveCaptain()) return@Button
            nav.open(
                UniversalScreens.Anvil(
                AnvilGUI.Builder()
                    .title("${Space.of(-65)}:guild_name_menu:")
                    .itemLeft(renameItem)
                    .plugin(guiyPlugin)
                    .onClose { nav.back() }
                    .onComplete { player, guildName: String ->
                        player.changeStoredGuildName(guildName)
                        AnvilGUI.Response.close()
                    }
            ))
        }
    ) {
        Text("<gold><b>Change Guild Name".miniMsg(),
            "<yellow><b>Guild Name:</b> <yellow><i>${player.getGuildName()}".miniMsg(),
            "<yellow><b>Guild Owner:</b> <yellow><i>${player.name}".miniMsg(),
            "<yellow><b>Guild Level:</b> <yellow><i>${player.getGuildLevel()}".miniMsg(),
            "<yellow><b>Guild Members:</b> <yellow><i>${player.getGuildMemberCount()}".miniMsg(),
            "<yellow><b>Guild Balance:</b> <yellow><i>${player.getGuildBalance()}".miniMsg(),
            modifier = Modifier.size(2, 2)
        )
    }
}

@Composable
fun GuildUIScope.GuildLevelUpButton(modifier: Modifier = Modifier) {
    val guild = player.getGuildName()
    val isMaxLevel = guild.getGuildLevelUpCost() == null
    Button(
        enabled = player.canLevelUpGuild(),
        onClick = { player.levelUpGuild() },
        modifier = modifier) { enabled ->
        if (enabled)
            Text(
                "<red><b>Level up Guildrank".miniMsg(),
                "<gold>Next level-up will cost <b>${guild.getGuildLevelUpCost()} coins</b>.".miniMsg()
            )
        else if (isMaxLevel)
            Text(
                "<red><b><st>Level up Guildrank".miniMsg(),
                "<darkred>Your guild has reached the current max-level.".miniMsg()
            )
        else {
            Text(
                "<red><b><st>Level up Guildrank".miniMsg(),
                "<red>You need <b>${guild.getGuildLevelUpCost()} coins</b> in your".miniMsg(),
                "<red>guild balance to level up your guildrank.".miniMsg()
            )
        }
    }
}

@Composable
fun GuildUIScope.GuildHouseButton(modifier: Modifier = Modifier) {
    Button(modifier = modifier) {
        Text(
            "<gold><b><st>Guild Housing".miniMsg(), modifier = Modifier.size(2, 2),
            lore = arrayOf("<red>This feature is not yet implemented.".miniMsg())
        )
    }
}

@Composable
fun GuildUIScope.GuildRelationshipButton(modifier: Modifier = Modifier) {
    Button(modifier = modifier) {
        Text(
            "<dark_red><b><st>Guild Wars".miniMsg(), modifier = Modifier.size(2, 2),
            lore = arrayOf("<red>This feature is not yet implemented.".miniMsg())
        )
    }
}

@Composable
fun GuildUIScope.GuildDisbandButton(modifier: Modifier = Modifier) {
    Button(
        modifier = modifier,
        enabled = (player.isGuildOwner()),
        onClick = { nav.open(GuildScreen.Disband) }
    ) { enabled ->
        if (enabled)
            Text("<red><b>Disband Guild".miniMsg())
        else
            Text("<red><b><st>Disband Guild".miniMsg())


    }
}

@Composable
fun GuildUIScope.GuildLeaveButton(player: Player, modifier: Modifier) {
    Button(
        modifier = modifier,
        enabled = player.hasGuild() && !player.isGuildOwner(),
        onClick = {
            nav.open(GuildScreen.Leave)
        }) { enabled ->
        if (enabled)
            Text("<red><i>Leave Guild".miniMsg())
        else
            Text("<red><i><st>Leave Guild".miniMsg())

    }
}
