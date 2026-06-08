package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mineinabyss.features.guilds.extensions.deleteGuild
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
fun GuildUIScope.GuildDisbandScreen() = Chest(":space_-8::guild_disband_or_leave_menu:", Modifier.height(5.dp)) {
    Row(Modifier.offset(1.dp, 1.dp)) {
        ConfirmButton()
        Spacer(Modifier.width(1.dp))
        CancelButton()
    }
}

@Composable
fun GuildUIScope.ConfirmButton(modifier: Modifier = Modifier) = Button(
    modifier,
    onClick = {
        player.deleteGuild()
        owner.exit()
    }) {
    Text("<green><b>Confirm Guild Disbanding".miniMsg(), modifier = Modifier.size(3.dp, 3.dp))
}

@Composable
fun CancelButton(modifier: Modifier = Modifier) {
    val dispatcher = LocalBackGestureDispatcher.current
    Button(
        modifier,
        onClick = { dispatcher.onBack() }
    ) {
        Text("<red><b>Cancel Guild Disbanding".miniMsg(), modifier = Modifier.size(3.dp, 3.dp))
    }
}
