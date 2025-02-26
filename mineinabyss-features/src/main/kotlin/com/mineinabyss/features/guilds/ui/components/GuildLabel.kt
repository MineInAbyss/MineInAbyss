package com.mineinabyss.features.guilds.ui.components

import androidx.compose.runtime.Composable
import com.mineinabyss.features.guilds.extensions.toOfflinePlayer
import com.mineinabyss.features.guilds.ui.GuildUiState
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.PlayerHead
import com.mineinabyss.guiy.components.items.PlayerHeadType
import com.mineinabyss.guiy.modifiers.Modifier

@Composable
fun GuildLabel(guild: GuildUiState, modifier: Modifier) = Button {
    PlayerHead(
        guild.owner.uuid.toOfflinePlayer(),
        "<gold><b>Current Guild Info</b>",
        "<yellow><b>Guild Name:</b> <i>${guild.name}",
        "<yellow><b>Guild Owner:</b> <i>${guild.owner.name}",
        "<yellow><b>Guild Level:</b> <i>${guild.level}",
        "<yellow><b>Guild Members:</b> <i>${guild.memberCount}",
        type = PlayerHeadType.LARGE_CENTER,
        modifier = modifier
    )
}
