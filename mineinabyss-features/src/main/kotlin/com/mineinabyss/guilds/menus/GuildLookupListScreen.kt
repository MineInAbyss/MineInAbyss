package com.mineinabyss.guilds.menus

import androidx.compose.runtime.*
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
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.miniMsg
import net.wesjd.anvilgui.AnvilGUI

@Composable
fun GuildUIScope.GuildLookupListScreen(pageNumber: Int) {
    val queriedGuildList = queriedList ?: defaultList
    var pageNum by remember { mutableStateOf(pageNumber) }
    var guildPageList by remember { mutableStateOf(queriedGuildList[pageNum]) }

    GuildListButton(Modifier.at(2, 0), guildPageList)
    PreviousPageButton(Modifier.at(3, 5), pageNum) {
        pageNum--
        guildPageList = queriedGuildList[pageNum]
        nav.refresh()
    }
    NextPageButton(Modifier.at(5, 5), pageNum, queriedGuildList) {
        pageNum++
        guildPageList = queriedGuildList[pageNum]
        nav.refresh()
    }
    LookForGuildButton(Modifier.at(7,5)) {
        pageNum = 0
        guildPageList = queriedGuildList[pageNum]
        nav.refresh()
    }
    BackButton(Modifier.at(0, 5))
}

private val defaultList = displayGuildList().chunked(20)
private var queriedList: List<List<GuildJoin>>? = null

@Composable
fun GuildUIScope.GuildListButton(modifier: Modifier = Modifier, guildPageList: List<GuildJoin>) {
    queriedList = null
    Grid(modifier.size(5, 5)) {
        guildPageList.forEach { (guildName, joinType, guildLevel) ->
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
                        "<yellow><b>Guild Membercount:</b> <yellow><i>${owner.getGuildMemberCount()} / ${guildLevel * 5}".miniMsg(),
                        isFlat = true
                    )
                )
            }
        }
    }
}

@Composable
fun PreviousPageButton(modifier: Modifier = Modifier, pageNum: Int, onClick: () -> Unit) {
    Button(
        enabled = pageNum > 0,
        modifier = modifier.at(3, 5),
        onClick = onClick
    ) { Text("<yellow><b>Previous".miniMsg()) }
}

@Composable
fun NextPageButton(
    modifier: Modifier,
    pageNum: Int,
    queriedGuildList: List<List<GuildJoin>>,
    onClick: () -> Unit
) {
    Button(
        enabled = pageNum < (queriedGuildList.size - 1),
        modifier = modifier.at(5, 5),
        onClick = onClick
    ) { Text("<yellow><b>Next".miniMsg()) }
}

@Composable
fun GuildUIScope.LookForGuildButton(modifier: Modifier, onClick: () -> Unit) {
    Button(
        modifier = modifier.at(7, 5),
        onClick = {
            nav.open(
                UniversalScreens.Anvil(
                    AnvilGUI.Builder()
                        .title("${Space.of(-64)}${Space.of(1)}:guild_search_menu:")
                        .itemLeft(TitleItem.of("Guild Name"))
                        .plugin(guiyPlugin)
                        .onClose { nav.back() }
                        .onComplete { player, guildName: String ->
                            val guilds = displayGuildList(guildName)
                            if (guilds.isEmpty()) player.error("No guild found with that name")
                            else queriedList = guilds.chunked(20)
                            run(onClick)
                            AnvilGUI.Response.close()
                        }
                ))
        }
    ) { Text("<gold><b>Search for a Guild by name".miniMsg()) }
}
