package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.*
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.features.helpers.di.Features
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.HorizontalGrid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.lists.NavbarPosition
import com.mineinabyss.guiy.components.lists.Paginated
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.event.ClickCallback

@Composable
fun GuildUIScope.GuildLookupListScreen() {
    var pageNum by remember { mutableStateOf(0) }
    var guildPageList by remember { mutableStateOf(displayGuildList()) }

    Paginated(
        guildPageList, pageNum,
        nextButton = { NextPageButton(Modifier.at(5, 0)) { pageNum++ } },
        previousButton = { PreviousPageButton(Modifier.at(3, 0)) { pageNum-- } },
        NavbarPosition.BOTTOM, null
    ) { pageItems ->
        HorizontalGrid(Modifier.at(1, 0).size(7, 5)) {
            pageItems.forEach { (guildName, joinType, guildLevel) ->
                val owner = guildName.getOwnerFromGuildName()
                Button(
                    onClick = {
                        if (player.hasGuild() && player.getGuildName().equals(guildName, true))
                            nav.open(GuildScreen.MemberList(guildLevel, player))
                        else
                            nav.open(GuildScreen.GuildLookupMembers(guildName))

                    }) {
                    Item(
                        TitleItem.head(
                            owner, "<gold><i>$guildName".miniMsg(),
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

    LookForGuildButton(Modifier.at(7,5)) { text ->
        pageNum = 0
        guildPageList = displayGuildList(text)
    }
    BackButton(Modifier.at(0, 5))
}

@Composable
fun PreviousPageButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        modifier = modifier,
        onClick = onClick
    ) { Text("<yellow><b>Previous".miniMsg()) }
}

@Composable
fun NextPageButton(
    modifier: Modifier,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = onClick
    ) { Text("<yellow><b>Next".miniMsg()) }
}

@Composable
fun GuildUIScope.LookForGuildButton(modifier: Modifier, onClick: (String) -> Unit) {
    Button(
        modifier = modifier.at(7, 5),
        onClick = {
            val maxGuildLength = Features.guilds.config.guildNameMaxLength
            val dialog = GuildDialogs(":space_-28::guild_search_menu:", "<gold>Search for Guild...", listOf(
                DialogInput.text("guild_dialog", "<gold>Search for guilds with name...".miniMsg())
                    .initial("Guild Name").width(maxGuildLength * 10)
                    .maxLength(maxGuildLength)
                    .build()
            )).createGuildLookDialog(onClick)

            player.showDialog(dialog)
        }
    ) { Text("<gold><b>Search for a Guild by name".miniMsg()) }
}
