package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guilds.database.GuildJoinType
import com.mineinabyss.guilds.extensions.*
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.messaging.error
import org.bukkit.ChatColor.*
import org.bukkit.OfflinePlayer

@Composable
fun GuildUIScope.GuildInviteScreen(owner: OfflinePlayer) {
    GuildLabel(owner, Modifier.at(4, 0))
    Row(Modifier.at(1, 2)) {
        AcceptGuildInvite(owner)
        Spacer(width = 1)
        DeclineGuildInvite(owner)
    }
    BackButton(Modifier.at(4, 4))
}

@Composable
fun GuildUIScope.GuildLabel(owner: OfflinePlayer, modifier: Modifier) = Button {
    Text(
        "$GOLD${BOLD}Current Guild Info:",
        "$YELLOW${BOLD}Guild Name: $YELLOW$ITALIC${owner.getGuildName()}",
        "$YELLOW${BOLD}Guild Owner: $YELLOW$ITALIC${owner.name}",
        "$YELLOW${BOLD}Guild Level: $YELLOW$ITALIC${owner.getGuildLevel()}",
        "$YELLOW${BOLD}Guild Members: $YELLOW$ITALIC${owner.getGuildMemberCount()}",
        modifier = modifier.size(2, 2)
    )
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
    Text("${GREEN}Accept Invite", modifier = Modifier.size(3, 2))
}

@Composable
fun GuildUIScope.DeclineGuildInvite(owner: OfflinePlayer) = Button(
    onClick = {
        player.removeGuildQueueEntries(GuildJoinType.Invite)
        player.sendMessage("$YELLOW${BOLD}❌ ${YELLOW}You denied the invite from $GOLD$ITALIC${owner.getGuildName()}")
        nav.back()
    }
) {
    Text("${RED}Decline Invite", modifier = Modifier.size(3, 2))
}
