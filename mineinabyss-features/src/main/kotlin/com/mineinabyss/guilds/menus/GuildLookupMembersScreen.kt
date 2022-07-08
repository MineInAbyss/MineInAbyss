package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guilds.database.GuildJoinType
import com.mineinabyss.guilds.extensions.*
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.head
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.messaging.miniMsg
import org.bukkit.OfflinePlayer

@Composable
fun GuildUIScope.GuildLookupMembersScreen(guildName: String) {
    val owner = guildName.getOwnerFromGuildName()
    val height = owner.getGuildLevel()?.plus(2) ?: 3
    GuildLabel(Modifier.at(4, 0), owner)
    GuildMembersButton(Modifier.at(1, 1), guildName)
    BackButton(Modifier.at(0, height))
    RequestToJoinButton(Modifier.at(4, height), owner, guildName)
}

@Composable
fun GuildLabel(modifier: Modifier, owner: OfflinePlayer) {
    Item(owner.head("<yellow><i>${owner.name}".miniMsg(), isCenterOfInv = true, isLarge = true), modifier = modifier)
}

@Composable
fun GuildUIScope.GuildMembersButton(modifier: Modifier, guildName: String) {
    Grid(modifier.size(5, guildLevel)) {
        guildName.getGuildMembers().sortedWith(compareBy { it.first; it.second.name }).forEach { (rank, member) ->
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
    val inviteOnly = owner.getGuildJoinType() == GuildJoinType.Invite
    Button(modifier = modifier, onClick = {
        if (!inviteOnly && !player.hasGuild())
            player.requestToJoin(guildName)
    }) {
        if (!inviteOnly && !player.hasGuild()) {
            Text("<green>Request to join <dark_green><i>$guildName".miniMsg())
        }
        else if (inviteOnly) {
            Text("<red><st>Request to join <i>$guildName".miniMsg(),
                "<dark_red><i>This guild can currently only".miniMsg(),
                "<dark_red><i>be joined by invites.".miniMsg()
            )
        }
        else if (player.hasGuild()) {
            Text("<red><st>Request to join <i>$guildName".miniMsg(),
                "<dark_red><i>You have to leave your Guild".miniMsg(),
                "<dark_red><i>before requesting to join another.".miniMsg()
            )
        }
    }
}
