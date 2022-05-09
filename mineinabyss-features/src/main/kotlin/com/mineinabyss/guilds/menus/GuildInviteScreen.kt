package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guilds.database.GuildJoinType
import com.mineinabyss.guilds.extensions.*
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.head
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.miniMsg
import org.bukkit.ChatColor.*
import org.bukkit.OfflinePlayer

@Composable
fun GuildUIScope.GuildInviteScreen(owner: OfflinePlayer) {
    GuildLabel(owner, Modifier.at(4, 0))
    AcceptGuildInvite(owner, Modifier.at(1, 1))
    DeclineGuildInvite(owner, Modifier.at(5, 1))
    BackButton(Modifier.at(4, 4))
}

@Composable
fun GuildUIScope.GuildLabel(owner: OfflinePlayer, modifier: Modifier) = Button {
    Item(owner.head(
        "<gold><b>Current Guild Info:",
        "<yellow><b>Guild Name: <yellow><i>${owner.getGuildName()}".miniMsg(),
        "<yellow><b>Guild Owner: <yellow><i>${owner.name}".miniMsg(),
        "<yellow><b>Guild Level: <yellow><i>${owner.getGuildLevel()}".miniMsg(),
        "<yellow><b>Guild Members: <yellow><i>${owner.getGuildMemberCount()}".miniMsg(),
        isCenterOfInv = true, isLarge = true
    ), modifier = modifier)
}

@Composable
fun GuildUIScope.AcceptGuildInvite(owner: OfflinePlayer, modifier: Modifier = Modifier) = Button(
    onClick = {
        if (owner.getGuildJoinType() == GuildJoinType.Request) {
            player.error("This guild is in 'Request-only' mode.")
            player.error("Change it to 'Any' or 'Invite-only' mode to accept invites.")
            return@Button
        }
        owner.addMemberToGuild(player)
        if (owner.getGuildMemberCount() >= guildLevel * 5 + 1) {
            player.error("This guild has reached its current member cap!")
            return@Button
        }
        player.removeGuildQueueEntries(GuildJoinType.Request)
        nav.back()
    },
    modifier = modifier
) {
    Text("<green>Accept Invite", modifier = Modifier.size(3, 3))
}

@Composable
fun GuildUIScope.DeclineGuildInvite(owner: OfflinePlayer, modifier: Modifier = Modifier) = Button(
    onClick = {
        player.removeGuildQueueEntries(GuildJoinType.Invite)
        player.info("$YELLOW<b>❌ <yellow>You denied the invite from $GOLD$ITALIC${owner.getGuildName()}")
        nav.back()
    }
) {
    Text("<red>Decline Invite", modifier = modifier.size(3, 3))
}
