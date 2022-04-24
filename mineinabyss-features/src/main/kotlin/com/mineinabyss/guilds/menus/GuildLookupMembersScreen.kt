package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guilds.database.GuildJoinType
import com.mineinabyss.guilds.extensions.getGuildMembers
import com.mineinabyss.guilds.extensions.getOwnerFromGuildName
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.head
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.mineinabyss.extensions.getGuildJoinType
import com.mineinabyss.mineinabyss.extensions.getGuildLevel
import com.mineinabyss.mineinabyss.extensions.hasGuild
import com.mineinabyss.mineinabyss.extensions.requestToJoin
import org.bukkit.ChatColor.*
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
fun GuildUIScope.GuildLabel(modifier: Modifier, owner: OfflinePlayer) {
    Item(owner.head("${YELLOW}${ITALIC}${owner.name}", isCenterOfInv = true, isLarge = true), modifier = modifier)
}

@Composable
fun GuildUIScope.GuildMembersButton(modifier: Modifier, guildName: String) {
    Grid(modifier.size(5, guildLevel)) {
        guildName.getGuildMembers().sortedBy { it.first; it.second.name }.forEach { (rank, member) ->
            Button {
                Item(
                    member.head(
                        "${GOLD}${ITALIC}${member.name}",
                        "${YELLOW}${BOLD}Guild Rank: ${YELLOW}${ITALIC}${rank}",
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
            Text("${GREEN}Request to join ${DARK_GREEN}${ITALIC}$guildName")
        }
        else if (inviteOnly) {
            Text("${RED}${STRIKETHROUGH}Request to join ${ITALIC}$guildName",
                "${DARK_RED}${ITALIC}This guild can currently only",
                "${DARK_RED}${ITALIC}be joined by invites."
            )
        }
        else if (player.hasGuild()) {
            Text("${RED}${STRIKETHROUGH}Request to join ${ITALIC}$guildName",
                "${DARK_RED}${ITALIC}You have to leave your Guild",
                "${DARK_RED}${ITALIC}before requesting to join another."
            )
        }
    }
}