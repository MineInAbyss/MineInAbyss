package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.mineinabyss.features.guilds.database.GuildJoinType
import com.mineinabyss.features.guilds.database.GuildRank
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.VerticalGrid
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.components.canvases.MAX_CHEST_HEIGHT
import com.mineinabyss.guiy.components.lists.NavbarPosition
import com.mineinabyss.guiy.components.lists.ScrollDirection
import com.mineinabyss.guiy.components.lists.Scrollable
import com.mineinabyss.guiy.components.lists.rememberScrollableState
import com.mineinabyss.idofront.textcomponents.miniMsg
import me.dvyy.compose.mini.layout.modifiers.height
import me.dvyy.compose.mini.layout.modifiers.offset
import me.dvyy.compose.mini.layout.modifiers.size
import me.dvyy.compose.mini.modifier.Modifier
import io.papermc.paper.datacomponent.item.ResolvableProfile
import org.bukkit.OfflinePlayer

@Composable
fun GuildUIScope.GuildLookupMembersScreen(guildName: String) = Chest(":space_-8:${":guild_lookup_members${minOf(guildName.getGuildLevel(), 3)}"}:", Modifier.height((guildName.getGuildLevel() + 3).coerceAtMost(MAX_CHEST_HEIGHT).dp)) {
    val owner = guildName.getOwnerFromGuildName()
    val guildLevel = owner.getGuildLevel()
    val height = minOf(guildLevel.plus(2), MAX_CHEST_HEIGHT - 1)
    val guildMembers = remember { guildName.getGuildMembers().sortedWith(compareBy { it.player.isConnected; it.player.name; it.rank.ordinal }).filter { it.rank != GuildRank.OWNER } }

    val scrollState = rememberScrollableState(ScrollDirection.VERTICAL)
    Scrollable(guildMembers, scrollState, NavbarPosition.END) { members ->
        VerticalGrid(Modifier.offset(2.dp, 1.dp).size(5.dp, minOf(guildLevel + 1, 4).dp)) {
            members.forEach { (rank, member) ->
                Button {
                    val profile = ResolvableProfile.resolvableProfile().uuid(member.uniqueId).build()
                    Item(
                        TitleItem.head(
                            profile, "<gold><i>${profile.name()}".miniMsg(),
                            "<yellow><b>Guild Rank: <yellow><i>$rank".miniMsg(),
                            isFlat = true
                        )
                    )
                }
            }
        }
    }

    GuildLabel(Modifier.offset(4.dp, 0.dp), owner)
    BackButton(Modifier.offset(0.dp, height.dp))
    RequestToJoinButton(Modifier.offset(4.dp, height.dp), owner, guildName)
}

@Composable
fun GuildLabel(modifier: Modifier, owner: OfflinePlayer) {
    val profile = ResolvableProfile.resolvableProfile().uuid(owner.uniqueId).build()
    Item(
        TitleItem.head(
            profile, "<gold><i>${profile.name()}".miniMsg(),
            "<yellow><b>Guild Rank: <yellow><i>${owner.getGuildRank()}".miniMsg(),
            isFlat = true, isCenterOfInv = true, isLarge = true
        ), modifier = modifier
    )
}

@Composable
fun GuildUIScope.RequestToJoinButton(modifier: Modifier, owner: OfflinePlayer, guildName: String) {
    val inviteOnly = owner.getGuildJoinType() == GuildJoinType.INVITE
    Button(modifier = modifier, onClick = {
        if (!inviteOnly && !player.hasGuild())
            player.requestToJoin(guildName)
    }) {
        if (!inviteOnly && !player.hasGuild()) {
            Text("<green>REQUEST to join <dark_green><i>$guildName".miniMsg())
        } else if (inviteOnly) {
            Text(
                "<red><st>REQUEST to join <i>$guildName".miniMsg(),
                "<dark_red><i>This guild can currently only".miniMsg(),
                "<dark_red><i>be joined via an invite.".miniMsg()
            )
        } else if (player.hasGuild()) {
            Text(
                "<red><st>REQUEST to join <i>$guildName".miniMsg(),
                "<dark_red><i>You have to leave your Guild".miniMsg(),
                "<dark_red><i>before requesting to join another.".miniMsg()
            )
        }
    }
}
