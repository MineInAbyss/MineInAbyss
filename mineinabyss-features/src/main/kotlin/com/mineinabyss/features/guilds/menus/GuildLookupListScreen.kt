package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.HorizontalGrid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.components.lists.NavbarPosition
import com.mineinabyss.guiy.components.lists.ScrollDirection
import com.mineinabyss.guiy.components.lists.Scrollable
import com.mineinabyss.guiy.components.lists.rememberScrollableState
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.datacomponent.item.ResolvableProfile
import io.papermc.paper.registry.data.dialog.input.DialogInput
import me.dvyy.compose.mini.layout.modifiers.height
import me.dvyy.compose.mini.layout.modifiers.offset
import me.dvyy.compose.mini.layout.modifiers.size
import me.dvyy.compose.mini.modifier.Modifier

@Composable
fun GuildUIScope.GuildLookupListScreen(
    onNavigateToMemberList: () -> Unit,
    onNavigateToLookupMembers: (guildName: String) -> Unit,
) = Chest(":space_-8::guild_list_menu:", Modifier.height(6.dp)) {
    var guildPageList by remember { mutableStateOf(displayGuildList()) }
    val scrollState = rememberScrollableState(ScrollDirection.PAGINATED)
    Scrollable(
        guildPageList,
        scrollState,
        NavbarPosition.BOTTOM
    ) { pageItems ->
        HorizontalGrid(Modifier.offset(1.dp, 0.dp).size(7.dp, 5.dp)) {
            pageItems.forEach { (guildName, joinType, guildLevel) ->
                val owner = guildName.getOwnerFromGuildName()
                Button(
                    onClick = {
                        if (player.hasGuild() && player.getGuildName().equals(guildName, true))
                            onNavigateToMemberList()
                        else
                            onNavigateToLookupMembers(guildName)

                    }) {
                    val profile = ResolvableProfile.resolvableProfile().uuid(owner.uniqueId).build()
                    Item(
                        TitleItem.head(
                            profile, "<gold><i>$guildName".miniMsg(),
                            "<yellow><b>Guild Owner:</b> <yellow><i>${profile.name()}".miniMsg(),
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

    LookForGuildButton(Modifier.offset(7.dp, 5.dp)) { text ->
        scrollState.setPage(0)
        guildPageList = displayGuildList(text)
    }
    BackButton(Modifier.offset(0.dp, 5.dp))
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
fun GuildUIScope.LookForGuildButton(modifier: Modifier, onClick: (String) -> Unit) {
    Button(
        modifier = modifier.offset(7.dp, 5.dp),
        onClick = {
            val maxGuildLength = abyss.guilds.config.guildNameMaxLength
            val dialog = GuildDialogs(
                ":space_-28::guild_search_menu:", "<gold>Search for Guild...", listOf(
                    DialogInput.text("guild_dialog", "<gold>Search for guilds with name...".miniMsg())
                        .initial("Guild Name").width(maxGuildLength * 10)
                        .maxLength(maxGuildLength)
                        .build()
                )
            ).createGuildLookDialog(onClick)

            player.showDialog(dialog)
        }
    ) { Text("<gold><b>Search for a Guild by name".miniMsg()) }
}
