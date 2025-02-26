package com.mineinabyss.features.guilds.ui.screens

import androidx.compose.runtime.Composable
import com.mineinabyss.features.guilds.extensions.deleteGuild
import com.mineinabyss.features.guilds.ui.GuildViewModel
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.inventory.CurrentPlayer
import com.mineinabyss.guiy.inventory.viewModel
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import org.bukkit.entity.Player

@Composable
fun GuildDisbandScreen(player: Player = CurrentPlayer, guild: GuildViewModel = viewModel()) {
    Row(Modifier.at(1, 1)) {
        Button(
            onClick = {
                player.deleteGuild()
                player.closeInventory()
            }) {
            Text("<green><b>Confirm Guild Disbanding", modifier = Modifier.size(3, 3))
        }
        Spacer(width = 1)
        Button(onClick = { guild.nav.back() }) {
            Text("<red><b>Cancel Guild Disbanding", modifier = Modifier.size(3, 3))
        }
    }
}
