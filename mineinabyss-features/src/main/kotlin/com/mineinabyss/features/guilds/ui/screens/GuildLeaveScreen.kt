package com.mineinabyss.features.guilds.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mineinabyss.features.guilds.extensions.leaveGuild
import com.mineinabyss.features.guilds.ui.BackButton
import com.mineinabyss.features.guilds.ui.GuildViewModel
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.inventory.CurrentPlayer
import com.mineinabyss.guiy.viewmodel.viewModel
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import org.bukkit.entity.Player

@Composable
fun GuildLeaveScreen(
    guildViewModel: GuildViewModel = viewModel(),
    onLeave: () -> Unit,
) {
    val guild by guildViewModel.currentGuild.collectAsState()
    Row(Modifier.at(1, 1)) {
        Button(onClick = {
            guildViewModel.leaveGuild()
            onLeave()
        }) {
            Text(
                "<green><b>Leave <dark_green><i>${guild?.name}",
                modifier = Modifier.size(3, 3)
            )
        }

        Spacer(width = 1)
        BackButton {
            Text(
                "<red><b>Don't Leave <dark_red><i>${guild?.name}",
                modifier = Modifier.size(3, 3)
            )
        }
    }
}
