package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guilds.extensions.deleteGuild
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.ui.composables.Button
import org.bukkit.ChatColor.*

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
        player.closeInventory()
    }) {
    Text("${GREEN}${BOLD}Confirm Guild Disbanding", modifier = Modifier.size(3, 3))
}

@Composable
fun GuildUIScope.CancelButton(modifier: Modifier = Modifier) = Button(
    modifier,
    onClick = { nav.back() }
) {
    Text("${RED}${BOLD}Cancel Guild Disbanding", modifier = Modifier.size(3, 3))
}
