package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.VerticalGrid
import com.mineinabyss.guiy.components.canvases.CHEST_WIDTH
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.components.lists.NavbarPosition
import com.mineinabyss.guiy.components.lists.ScrollDirection
import com.mineinabyss.guiy.components.lists.Scrollable
import com.mineinabyss.guiy.components.lists.ScrollableState
import com.mineinabyss.guiy.components.lists.ScrollbarTrack
import com.mineinabyss.guiy.modifiers.scroll.onScroll
import com.mineinabyss.guiy.modifiers.scroll.scrollableWhenEmpty
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.registry.data.dialog.input.DialogInput
import me.dvyy.compose.mini.layout.jetpack.Box
import me.dvyy.compose.mini.layout.modifiers.height
import me.dvyy.compose.mini.layout.modifiers.offset
import me.dvyy.compose.mini.layout.modifiers.size
import me.dvyy.compose.mini.modifier.Modifier
import org.bukkit.inventory.ItemStack

private const val GRID_WIDTH = 8
private const val GRID_HEIGHT = 5
private const val CHEST_HEIGHT = GRID_HEIGHT + 1

private enum class GuildSort(val label: String, val comparator: Comparator<GuildListEntry>) {
    NAME("A-Z", compareBy { it.guildName.lowercase() }),
    MEMBERS("Most members", compareByDescending<GuildListEntry> { it.memberCount }.thenBy { it.guildName.lowercase() }),
    AGE("Oldest first", compareBy { it.id }),
}

@Composable
fun GuildUIScope.GuildLookupListScreen(
    onNavigateToMemberList: () -> Unit,
    onNavigateToLookupMembers: (guildName: String) -> Unit,
) = Chest(":space_-8::guild_list_menu:", Modifier.height(CHEST_HEIGHT.dp)) {
    var guilds by remember { mutableStateOf(displayGuildList()) }
    var sort by remember { mutableStateOf(GuildSort.NAME) }
    val sorted = remember(guilds, sort) { guilds.sortedWith(sort.comparator) }
    // A new search or sort lands on the first row
    val state = remember(sorted) { ScrollableState(ScrollDirection.VERTICAL) }

    Box(Modifier.size(CHEST_WIDTH.dp, CHEST_HEIGHT.dp).onScroll { state.scroll(delta) }.scrollableWhenEmpty()) {
        Box(Modifier.size(GRID_WIDTH.dp, GRID_HEIGHT.dp)) {
            Scrollable(sorted, state = state, navbarPosition = NavbarPosition.END, scrollbar = {}) { page ->
                VerticalGrid(Modifier.size(GRID_WIDTH.dp, GRID_HEIGHT.dp)) {
                    page.forEach { guild ->
                        Button(onClick = {
                            if (player.hasGuild() && player.getGuildName().equals(guild.guildName, true)) onNavigateToMemberList()
                            else onNavigateToLookupMembers(guild.guildName)
                        }) {
                            Item(guildHead(guild))
                        }
                    }
                }
            }
        }
        ScrollbarTrack(state, CHEST_HEIGHT, TitleItem.scrollbarModel, Modifier.offset(GRID_WIDTH.dp, 0.dp))
        BackButton(Modifier.offset(0.dp, GRID_HEIGHT.dp))
        LookForGuildButton(Modifier.offset((GRID_WIDTH / 2).dp, GRID_HEIGHT.dp)) { guilds = displayGuildList(it) }
        Button(Modifier.offset((GRID_WIDTH - 1).dp, GRID_HEIGHT.dp), onClick = { sort = GuildSort.entries[(sort.ordinal + 1) % GuildSort.entries.size] }) {
            Text("<gold><b>Sort: <yellow>${sort.label}".miniMsg(), "<gray>Click to change".miniMsg())
        }
    }
}

private fun guildHead(guild: GuildListEntry): ItemStack {
    val title = "<gold><i>${guild.guildName}".miniMsg()
    val lore = arrayOf(
        "<yellow><b>Guild Owner:</b> <yellow><i>${guild.ownerName}".miniMsg(),
        "<yellow><b>Guild Level:</b> <yellow><i>${guild.guildLevel}".miniMsg(),
        "<yellow><b>Guild Jointype:</b> <yellow><i>${guild.joinType}".miniMsg(),
        "<yellow><b>Guild Membercount:</b> <yellow><i>${guild.memberCount} / ${guild.guildLevel * 5}".miniMsg(),
    )
    return if (guild.owner != null) TitleItem.head(guild.owner, title, *lore, isFlat = true)
    else TitleItem.of(title, *lore)
}

@Composable
fun GuildUIScope.LookForGuildButton(modifier: Modifier, onClick: (String) -> Unit) {
    Button(
        modifier = modifier,
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
