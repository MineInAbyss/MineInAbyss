package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mineinabyss.features.guilds.extensions.leaveGuild
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.navigation.LocalBackGestureDispatcher
import com.mineinabyss.idofront.textcomponents.miniMsg
import me.dvyy.compose.mini.layout.jetpack.Row
import me.dvyy.compose.mini.layout.modifiers.height
import me.dvyy.compose.mini.layout.modifiers.offset
import me.dvyy.compose.mini.layout.modifiers.size
import me.dvyy.compose.mini.layout.modifiers.width
import me.dvyy.compose.mini.modifier.Modifier

@Composable
fun GuildUIScope.GuildLeaveScreen() = Chest(":space_-8::guild_disband_or_leave_menu:", Modifier.height(5.dp)) {
    Row(Modifier.offset(1.dp, 1.dp)) {
        LeaveButton()
        Spacer(Modifier.width(1.dp))
        DontLeaveButton()
    }
}

@Composable
fun GuildUIScope.LeaveButton(modifier: Modifier = Modifier) = Button(
    modifier,
    onClick = {
        player.leaveGuild()
        owner.exit()
    }) {
    Text(
        "<green><b>Leave <dark_green><i>${guildName}".miniMsg(),
        modifier = Modifier.size(3.dp, 3.dp)
    )
}

@Composable
fun GuildUIScope.DontLeaveButton(modifier: Modifier = Modifier) {
    val dispatcher = LocalBackGestureDispatcher.current
    Button(
        modifier,
        onClick = { dispatcher.onBack() }
    ) {
        Text(
            "<red><b>Don't Leave <dark_red><i>${guildName}".miniMsg(),
            modifier = Modifier.size(3.dp, 3.dp)
        )
    }
}
