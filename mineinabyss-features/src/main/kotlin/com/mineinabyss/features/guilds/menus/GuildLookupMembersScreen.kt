package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.features.guilds.database.GuildJoinType
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.head
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.MAX_CHEST_HEIGHT
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.OfflinePlayer

@Composable
fun GuildUIScope.GuildLookupMembersScreen(guildName: String) {
    val owner = guildName.getOwnerFromGuildName()
    val guildLevel = owner.getGuildLevel()
    val height = minOf(guildLevel.plus(2), MAX_CHEST_HEIGHT - 1)

    GuildLabel(Modifier.at(4, 0), owner)
    GuildMembersButton(Modifier.at(2, 1), guildName, guildLevel)
    BackButton(Modifier.at(0, height))
    RequestToJoinButton(Modifier.at(4, height), owner, guildName)
}

@Composable
fun GuildLabel(modifier: Modifier, owner: OfflinePlayer) {
    Item(owner.head("<yellow><i>${owner.name}".miniMsg(), isCenterOfInv = true, isLarge = true), modifier = modifier)
}

@Composable
fun GuildMembersButton(modifier: Modifier, guildName: String, guildLevel: Int) {
    val members = guildName.getGuildMembers().sortedWith(compareBy {  it.player.name; it.rank.ordinal })
    Grid(modifier.size(5, guildLevel + 1)) {
        members.forEach { (rank, member) ->
            Button {
                Item(
                    member.head(
                        "<gold><i>${member.name}".miniMsg(),
                        "<yellow><b>Guild Rank: <yellow><i>$rank".miniMsg(),
                        isFlat = true
                    )
                )
            }
        }
    }
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
        }
        else if (inviteOnly) {
            Text("<red><st>REQUEST to join <i>$guildName".miniMsg(),
                "<dark_red><i>This guild can currently only".miniMsg(),
                "<dark_red><i>be joined via an invite.".miniMsg()
            )
        }
        else if (player.hasGuild()) {
            Text("<red><st>REQUEST to join <i>$guildName".miniMsg(),
                "<dark_red><i>You have to leave your Guild".miniMsg(),
                "<dark_red><i>before requesting to join another.".miniMsg()
            )
        }
    }
}
