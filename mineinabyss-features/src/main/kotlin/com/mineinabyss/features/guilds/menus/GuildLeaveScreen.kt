package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mineinabyss.features.guilds.extensions.leaveGuild
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.inventory.viewModel
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size

@Composable
fun GuildViewModel.GuildLeaveScreen(guildViewModel: GuildViewModel = viewModel()) {
    val guild by guildViewModel.guildUiState.collectAsState()
    Row(Modifier.at(1, 1)) {
        Button(onClick = {
            player.leaveGuild()
            player.closeInventory()
        }) {
            Text(
                "<green><b>Leave <dark_green><i>${guild?.name}",
                modifier = Modifier.size(3, 3)
            )
        }

        Spacer(width = 1)
        Button(onClick = { nav.back() }) {
            Text(
                "<red><b>Don't Leave <dark_red><i>${guild?.name}",
                modifier = Modifier.size(3, 3)
            )
        }
    }
}
