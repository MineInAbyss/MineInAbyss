package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guilds.extensions.*
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.navigation.UniversalScreens
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.head
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.messaging.miniMsg
import net.wesjd.anvilgui.AnvilGUI

@Composable
fun GuildUIScope.GuildLookupListScreen() {
    GuildListButton(Modifier.at(2, 0))
    BackButton(Modifier.at(0, 5))
    PreviousButton(Modifier.at(3, 5))
    NextButton(Modifier.at(5, 5))
    LookForGuildButton(Modifier.at(7, 5))
}

//TODO Fix Next/Previous buttons
@Composable
fun GuildUIScope.GuildListButton(modifier: Modifier = Modifier) {
    Grid(modifier.size(5, 5)) {
        displayGuildList().forEach { (guildName, joinType, guildLevel) ->
            val owner = guildName.getOwnerFromGuildName()
            Button(
                onClick = {
                    if (player.hasGuild() && player.getGuildName().lowercase() == guildName.lowercase())
                        nav.open(GuildScreen.MemberList(guildLevel, player))
                    else
                        nav.open(GuildScreen.GuildLookupMembers(guildName))

                }) {
                Item(
                    owner.head(
                        "<gold><i>$guildName".miniMsg(),
                        "<yellow><b>Guild Owner:</b> <yellow><i>${owner.name}".miniMsg(),
                        "<yellow><b>Guild Level:</b> <yellow><i>${guildLevel}".miniMsg(),
                        "<yellow><b>Guild Jointype:</b> <yellow><i>${joinType}".miniMsg(),
                        "<yellow><b>Guild Membercount:</b> <yellow><i>${owner.getGuildMemberCount()} / ${
                            owner.getGuildLevel()?.times(5)
                        }".miniMsg(),
                        isFlat = true
                    )
                )
            }
        }
    }
}

@Composable
fun PreviousButton(modifier: Modifier = Modifier) {
    Button(
        modifier = modifier,
        onClick = {
        }
    ) {
        Text(
            "<yellow><b>Previous".miniMsg(),
            "<red>This feature is not yet implemented.".miniMsg()
        )
    }
}

@Composable
fun NextButton(modifier: Modifier = Modifier) {
    Button(
        modifier = modifier,
        onClick = {
            displayGuildList()
        }) {
        Text(
            "<yellow><b>Next".miniMsg(),
            "<red>This feature is not yet implemented.".miniMsg()
        )
    }
}

@Composable
fun GuildUIScope.LookForGuildButton(modifier: Modifier) {
    val button = TitleItem.of("Guild Name")
    Button(
        modifier = modifier,
        onClick = {
            nav.open(
                UniversalScreens.Anvil(
                    AnvilGUI.Builder()
                        .title("${Space.of(-64)}${Space.of(1)}:guild_search_menu:")
                        .itemLeft(button)
                        .plugin(guiyPlugin)
                        .onClose { nav.back() }
                        .onComplete { player, guildName: String ->
                            if (player.hasGuild() && player.getGuildName().lowercase() == guildName.lowercase())
                                nav.open(GuildScreen.MemberList(guildLevel, player))
                            else if (player.verifyGuildName(guildName) != null)
                                nav.open(GuildScreen.GuildLookupMembers(guildName))
                            AnvilGUI.Response.close()
                        }
                ))
        }
    ) {
        Text("<gold><b>Search for a Guild by name".miniMsg())
    }
}

