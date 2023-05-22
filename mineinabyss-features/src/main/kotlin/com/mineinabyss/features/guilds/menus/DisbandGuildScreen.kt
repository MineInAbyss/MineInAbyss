package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.features.guilds.extensions.deleteGuild
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.textcomponents.miniMsg

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
    Text("<green><b>Confirm Guild Disbanding".miniMsg(), modifier = Modifier.size(3, 3))
}

@Composable
fun GuildUIScope.CancelButton(modifier: Modifier = Modifier) = Button(
    modifier,
    onClick = { nav.back() }
) {
    Text("<red><b>Cancel Guild Disbanding".miniMsg(), modifier = Modifier.size(3, 3))
}
