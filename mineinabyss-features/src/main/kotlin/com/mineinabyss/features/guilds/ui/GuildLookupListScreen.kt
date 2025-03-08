package com.mineinabyss.features.guilds.ui

import androidx.compose.runtime.*
import com.mineinabyss.features.guilds.extensions.displayGuildList
import com.mineinabyss.features.guilds.extensions.getGuildMemberCount
import com.mineinabyss.features.guilds.extensions.getOwnerFromGuildName
import com.mineinabyss.guiy.components.HorizontalGrid
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.PlayerHead
import com.mineinabyss.guiy.components.items.PlayerHeadType
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.components.lists.NavbarPosition
import com.mineinabyss.guiy.components.lists.Paginated
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.textcomponents.miniMsg

@Composable
fun GuildLookupListScreen() {
    var pageNum by remember { mutableStateOf(0) }
    var guildPageList by remember { mutableStateOf(displayGuildList()) }

    Paginated(
        guildPageList, pageNum,
        onPageChange = { pageNum = it },
        nextButton = { NextPageButton(Modifier.at(5, 0)) },
        previousButton = { PreviousPageButton(Modifier.at(3, 0)) },
        NavbarPosition.BOTTOM, null
    ) { pageItems ->
        HorizontalGrid(Modifier.at(1, 0).size(7, 5)) {
            pageItems.forEach { (guildName, joinType, guildLevel) ->
                val owner = guildName.getOwnerFromGuildName()
                Button(
                    onClick = {
                        // TODO navigation
//                        if (player.hasGuild() && player.getGuildName().equals(guildName, true))
//                            nav.open(GuildScreen.MemberList(guildLevel, player))
//                        else
//                            nav.open(GuildScreen.GuildLookupMembers(guildName))

                    }) {
                    PlayerHead(
                        owner,
                        "<gold><i>$guildName",
                        "<yellow><b>Guild Owner:</b> <yellow><i>${owner.name}",
                        "<yellow><b>Guild Level:</b> <yellow><i>${guildLevel}",
                        "<yellow><b>Guild Jointype:</b> <yellow><i>${joinType}",
                        "<yellow><b>Guild Membercount:</b> <yellow><i>${owner.getGuildMemberCount()} / ${guildLevel * 5}",
                        type = PlayerHeadType.FLAT
                    )
                }
            }
        }
    }

    LookForGuildButton(Modifier.at(7, 5)) { text ->
        pageNum = 0
        guildPageList = displayGuildList(text)
    }
    BackButton(Modifier.at(0, 5))
}

@Composable
fun PreviousPageButton(modifier: Modifier = Modifier) {
    Button(
        modifier = modifier,
    ) { Text("<yellow><b>Previous".miniMsg()) }
}

@Composable
fun NextPageButton(
    modifier: Modifier,
) {
    Button(
        modifier = modifier,
    ) { Text("<yellow><b>Next".miniMsg()) }
}

@Composable
fun LookForGuildButton(modifier: Modifier, onClick: (String) -> Unit) {
    Button(
        modifier = modifier.at(7, 5),
        onClick = {
            // TODO open anvil menu
//            nav.open(
//                UniversalScreens.Anvil(
//                    AnvilGUI.Builder()
//                        .title(":space_-61::guild_search_menu:")
//                        .itemLeft(TitleItem.of("Guild Name").editItemMeta { isHideTooltip = true })
//                        .itemOutput(TitleItem.transparentItem)
//                        .plugin(guiyPlugin)
//                        .onClose { nav.back() }
//                        .onClick { _, snapshot ->
//                            onClick.invoke(snapshot.text)
//                            listOf(AnvilGUI.ResponseAction.close())
//                        }
//                ))
        }
    ) { Text("<gold><b>Search for a Guild by name".miniMsg()) }
}
