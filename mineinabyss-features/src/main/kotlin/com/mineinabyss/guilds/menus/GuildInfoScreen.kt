package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.layout.Column
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.mineinabyss.extensions.getGuildLevel
import com.mineinabyss.mineinabyss.extensions.getGuildMemberCount
import com.mineinabyss.mineinabyss.extensions.getGuildName
import com.mineinabyss.mineinabyss.extensions.getGuildOwner
import org.bukkit.Bukkit
import org.bukkit.ChatColor

@Composable
fun GuildUIScope.GuildInfoScreen() {
    Column(Modifier.at(2, 0)) {
        Row {
            GuildMemberManagement()
            Spacer(width = 1)
            CurrentGuildInfoButton()
        }
        Spacer(height = 1)
        Row {
            GuildHouseButton()
            Spacer(width = 1)
            GuildRelationshipButton()
        }
    }

    GuildLeaveButton(player, Modifier.at(8, 5))

    BackButton(Modifier.at(0, 5))
}

@Composable
fun GuildUIScope.CurrentGuildInfoButton(modifier: Modifier = Modifier) {
    Button(modifier = modifier) {
        Text(
            "${ChatColor.GOLD}${ChatColor.BOLD}Current Guild Info:",
            "${ChatColor.YELLOW}${ChatColor.BOLD}Guild Name: ${ChatColor.YELLOW}${ChatColor.ITALIC}${player.getGuildName()}",
            "${ChatColor.YELLOW}${ChatColor.BOLD}Guild Owner: ${ChatColor.YELLOW}${ChatColor.ITALIC}${
                Bukkit.getOfflinePlayer(player.getGuildOwner()).name}",
            "${ChatColor.YELLOW}${ChatColor.BOLD}Guild Level: ${ChatColor.YELLOW}${ChatColor.ITALIC}${player.getGuildLevel()}",
            "${ChatColor.YELLOW}${ChatColor.BOLD}Guild Members: ${ChatColor.YELLOW}${ChatColor.ITALIC}${player.getGuildMemberCount()}",
            modifier = Modifier.size(2, 2)
        )
    }
}