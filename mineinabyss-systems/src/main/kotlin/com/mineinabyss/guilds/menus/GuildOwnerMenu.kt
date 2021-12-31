package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.layout.Column
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.ui.UniversalScreens
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.mineinabyss.extensions.changeStoredGuildName
import com.mineinabyss.mineinabyss.extensions.getGuildName
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.ChatColor.*

@Composable
fun GuildUIScope.GuildOwnerScreen() {
    Column(Modifier.at(2, 1)) {
        Row {
            GuildMemberManagement()
            Spacer(width = 1)
            GuildRenameButton()
        }
        Spacer(height = 1)
        Row {
            GuildHouseButton()
            Spacer(width = 1)
            GuildRelationshipButton()
        }
    }

    GuildDisbandButton(Modifier.at(8, 5))
    BackButton(Modifier.at(0, 5))
}

@Composable
fun GuildUIScope.GuildMemberManagement(modifier: Modifier = Modifier) {
    Button(
        TitleItem.of("$GREEN${BOLD}Guild Member List"),
        modifier.size(2, 2).clickable {
            nav.open(GuildScreen.MemberList(guildLevel))
        }
    )
}

@Composable
fun GuildUIScope.GuildRenameButton(modifier: Modifier = Modifier) {
    Button(
        TitleItem.of("$YELLOW${BOLD}Change Guild Name"),
        modifier.size(2, 2).clickable {
            nav.open(UniversalScreens.Anvil(
                AnvilGUI.Builder()
                    .title(":guild_naming:")
                    .itemLeft(TitleItem.of(player.getGuildName()))
                    .plugin(guiyPlugin)
                    .onClose { nav.back() }
                    .onComplete { player, guildName: String ->
                        player.changeStoredGuildName(guildName)
                        AnvilGUI.Response.close()
                    }
            ))
        }
    )
}

@Composable
fun GuildUIScope.GuildHouseButton(modifier: Modifier = Modifier) {
    Button(
        TitleItem.of("$GOLD${BOLD}Change Guild House"),
        modifier.size(2, 2)
    )
}

@Composable
fun GuildUIScope.GuildRelationshipButton(modifier: Modifier = Modifier) {
    Button(
        TitleItem.of("$BLUE${BOLD}Guild Relationships"),
        modifier.size(2, 2).clickable {
//            nav.open(GuildScreen.Relationships)
        })
}

@Composable
fun GuildUIScope.GuildDisbandButton(modifier: Modifier = Modifier) {
    Button(
        TitleItem.of("$RED${BOLD}Disband Guild"),
        modifier.clickable {
            nav.open(GuildScreen.Disband)
        }
    )
}
