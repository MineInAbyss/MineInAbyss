package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.layout.Column
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.Bukkit

@Composable
fun GuildUIScope.GuildInfoScreen() {
    Column(Modifier.at(2, 0)) {
        Row {
            GuildMemberManagement()
            Spacer(width = 1)
            CurrentGuildInfoButton()
        }
        Spacer(height = 1)
        Row {
            GuildHouseButton()
            Spacer(width = 1)
            GuildRelationshipButton()
        }
    }

    GuildLeaveButton(player, Modifier.at(8, 5))

    BackButton(Modifier.at(0, 5))
}

@Composable
fun GuildUIScope.CurrentGuildInfoButton(modifier: Modifier = Modifier) {
    Button(modifier = modifier) {
        Text(
            "<gold><b>Current Guild Info</b>".miniMsg(),
            "<yellow><b>Guild Name:</b> <yellow><i>${guildName}".miniMsg(),
            "<yellow><b>Guild Owner:</b> <yellow><i>${guildOwner.name}".miniMsg(),
            "<yellow><b>Guild Level:</b> <yellow><i>${guildLevel}".miniMsg(),
            "<yellow><b>Guild Members:</b> <yellow><i>${memberCount}".miniMsg(),
            modifier = Modifier.size(2, 2)
        )
    }
}
