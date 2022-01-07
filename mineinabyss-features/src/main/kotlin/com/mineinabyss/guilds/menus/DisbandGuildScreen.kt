package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.mineinabyss.extensions.deleteGuild
import org.bukkit.ChatColor

@Composable
fun GuildUIScope.GuildDisbandScreen() {
    Row(Modifier.at(1, 1)) {
        ConfirmButton()
        Spacer(width = 1)
        CancelButton()
    }
}

@Composable
fun GuildUIScope.ConfirmButton(modifier: Modifier = Modifier) = Button(
    modifier,
    onClick = {
        player.deleteGuild()
        nav.reset()
    }) {
    Text("${ChatColor.GREEN}${ChatColor.BOLD}Confirm Guild Disbanding", modifier = Modifier.size(3, 2))
}

@Composable
fun GuildUIScope.CancelButton(modifier: Modifier = Modifier) = Button(
    modifier,
    onClick = { nav.back() }
) {
    Text("${ChatColor.RED}${ChatColor.BOLD}Cancel Guild Disbanding", modifier = Modifier.size(3, 2))
}
