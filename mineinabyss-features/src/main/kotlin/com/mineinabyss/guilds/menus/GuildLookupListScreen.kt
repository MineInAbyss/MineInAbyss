package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guilds.extensions.*
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.guiyPlugin
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.NoToolTip
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.head
import com.mineinabyss.helpers.ui.UniversalScreens
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.font.Space
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.ChatColor.*

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
                        "${GOLD}${ITALIC}${guildName}",
                        "${YELLOW}${BOLD}Guild Owner: ${YELLOW}${ITALIC}${owner.name}",
                        "${YELLOW}${BOLD}Guild Level: ${YELLOW}${ITALIC}${guildLevel}",
                        "${YELLOW}${BOLD}Guild Jointype: ${YELLOW}${ITALIC}${joinType}",
                        "${YELLOW}${BOLD}Guild Membercount: ${YELLOW}${ITALIC}${owner.getGuildMemberCount()} / ${
                            owner.getGuildLevel()?.times(5)
                        }",
                        isFlat = true
                    )
                )
            }
        }
    }
}

@Composable
fun GuildUIScope.PreviousButton(modifier: Modifier = Modifier) {
    Button(
        modifier = modifier,
        onClick = {
        }
    ) {
        Text(
            "${YELLOW}${BOLD}Previous",
            "${RED}This feature is not yet implemented."
        )
    }
}

@Composable
fun GuildUIScope.NextButton(modifier: Modifier = Modifier) {
    Button(
        modifier = modifier,
        onClick = {
            displayGuildList()
        }) {
        Text(
            "${YELLOW}${BOLD}Next",
            "${RED}This feature is not yet implemented."
        )
    }
}

@Composable
fun GuildUIScope.LookForGuildButton(modifier: Modifier) {
    val button = TitleItem.of("Guild Name").NoToolTip()
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
        Text("${GOLD}${BOLD}Search for a Guild by name")
    }
}

