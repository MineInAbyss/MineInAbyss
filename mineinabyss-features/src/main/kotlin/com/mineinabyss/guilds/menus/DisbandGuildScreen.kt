package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guilds.deleteGuild
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.ui.composables.Button
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
    TitleItem.of("${ChatColor.GREEN}${ChatColor.BOLD}Confirm Guild Disbanding"),
    modifier.size(3, 2).clickable {
        player.deleteGuild()
        nav.reset()
    }
)

@Composable
fun GuildUIScope.CancelButton(modifier: Modifier = Modifier) = Button(
    TitleItem.of("${ChatColor.RED}${ChatColor.BOLD}Cancel Guild Disbanding"),
    modifier.size(3, 2).clickable { nav.back() }
)
