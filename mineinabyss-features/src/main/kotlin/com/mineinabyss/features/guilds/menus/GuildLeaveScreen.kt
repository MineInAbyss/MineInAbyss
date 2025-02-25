package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.features.guilds.extensions.leaveGuild
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.textcomponents.miniMsg

@Composable
fun GuildViewModel.GuildLeaveScreen() {
    Row(Modifier.at(1, 1)) {
        LeaveButton()
        Spacer(width = 1)
        DontLeaveButton()
    }
}

@Composable
fun GuildViewModel.LeaveButton(modifier: Modifier = Modifier) = Button(
    onClick = {
        player.leaveGuild()
        player.closeInventory()
    }, modifier
) {
    Text(
        "<green><b>Leave <dark_green><i>${guildName}",
        modifier = Modifier.size(3, 3)
    )
}

@Composable
fun GuildViewModel.DontLeaveButton(modifier: Modifier = Modifier) = Button(
    onClick = { nav.back() }, modifier
) {
    Text(
        "<red><b>Don't Leave <dark_red><i>${guildName}",
        modifier = Modifier.size(3, 3)
    )
}
