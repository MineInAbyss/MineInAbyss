package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.mineinabyss.extensions.getGuildName
import com.mineinabyss.mineinabyss.extensions.leaveGuild
import org.bukkit.ChatColor

@Composable
fun GuildUIScope.GuildLeaveScreen() {
    Row(Modifier.at(1, 1)) {
        LeaveButton()
        Spacer(width = 1)
        DontLeaveButton()
    }
}

@Composable
fun GuildUIScope.LeaveButton(modifier: Modifier = Modifier) = Button(
    modifier,
    onClick = {
        player.leaveGuild()
        player.closeInventory()
    }) {
    Text(
        "${ChatColor.GREEN}${ChatColor.BOLD}Leave ${ChatColor.DARK_GREEN}${ChatColor.ITALIC}${player.getGuildName()}",
        modifier = Modifier.size(3, 3)
    )
}

@Composable
fun GuildUIScope.DontLeaveButton(modifier: Modifier = Modifier) = Button(
    modifier,
    onClick = { nav.back() }
) {
    Text(
        "${ChatColor.RED}${ChatColor.BOLD}Don't Leave ${ChatColor.DARK_RED}${ChatColor.ITALIC}${player.getGuildName()}",
        modifier = Modifier.size(3, 3)
    )
}
