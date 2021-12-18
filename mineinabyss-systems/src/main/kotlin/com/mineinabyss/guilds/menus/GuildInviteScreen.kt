package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.nodes.InventoryCanvasScope.at
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.mineinabyss.data.GuildJoinType
import com.mineinabyss.mineinabyss.extensions.*
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
fun GuildUIScope.GuildLabel(owner: OfflinePlayer, modifier: Modifier) = Button(
    TitleItem.of(
        "$GOLD${BOLD}Current Guild Info:",
        "$YELLOW${BOLD}Guild Name: $YELLOW$ITALIC${owner.getGuildName()}",
        "$YELLOW${BOLD}Guild Owner: $YELLOW$ITALIC${owner.name}",
        "$YELLOW${BOLD}Guild Level: $YELLOW$ITALIC${owner.getGuildLevel()}",
        "$YELLOW${BOLD}Guild Members: $YELLOW$ITALIC${owner.getGuildMemberCount()}"
    ),
    modifier.size(2, 2)
)

@Composable
fun GuildUIScope.AcceptGuildInvite(owner: OfflinePlayer, modifier: Modifier = Modifier) = Button(
    TitleItem.of("${GREEN}Accept Invite"),
    modifier.size(3, 2).clickable {
        if (owner.getGuildJoinType() == GuildJoinType.Request) {
            player.error("This guild is in 'Request-only' mode.")
            player.error("Change it to 'Any' or 'Invite-only' mode to accept invites.")
            return@clickable
        }
        owner.addMemberToGuild(player)
        if (owner.getGuildMemberCount() >= guildLevel * 5 + 1) {
            player.error("This guild has reached its current member cap!")
            return@clickable
        }
        player.removeGuildQueueEntries(GuildJoinType.Request)
        nav.back()
    }
)

@Composable
fun GuildUIScope.DeclineGuildInvite(owner: OfflinePlayer, modifier: Modifier = Modifier) = Button(
    TitleItem.of("${RED}Decline Invite"),
    modifier.size(3, 2).clickable {
        player.removeGuildQueueEntries(GuildJoinType.Invite)
        player.sendMessage("$YELLOW${BOLD}❌ ${YELLOW}You denied the invite from $GOLD$ITALIC${owner.getGuildName()}")
        nav.back()
    }
)
