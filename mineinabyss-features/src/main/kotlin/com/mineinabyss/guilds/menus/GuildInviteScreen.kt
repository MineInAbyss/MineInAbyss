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
import org.bukkit.OfflinePlayer

@Composable
fun GuildUIScope.GuildInviteScreen(owner: OfflinePlayer) {
    GuildLabel(owner, Modifier.at(4, 0))
    AcceptGuildInviteButton(owner, Modifier.at(1, 1))
    DeclineGuildInviteButton(owner, Modifier.at(5, 1))
    BackButton(Modifier.at(4, 4))
}

@Composable
fun GuildLabel(owner: OfflinePlayer, modifier: Modifier) = Button {
    Item(owner.head(
        "<gold><b>Current Guild Info</b>".miniMsg(),
        "<yellow><b>Guild Name:</b> <yellow><i>${owner.getGuildName()}".miniMsg(),
        "<yellow><b>Guild Owner:</b> <yellow><i>${owner.name}".miniMsg(),
        "<yellow><b>Guild Level:</b> <yellow><i>${owner.getGuildLevel()}".miniMsg(),
        "<yellow><b>Guild Members:</b> <yellow><i>${owner.getGuildMemberCount()}".miniMsg(),
        isCenterOfInv = true, isLarge = true
    ), modifier = modifier)
}

@Composable
fun GuildUIScope.AcceptGuildInviteButton(owner: OfflinePlayer, modifier: Modifier = Modifier) = Button(
    onClick = {
        if (!owner.hasGuild()) {
            nav.reset()
            player.error("This guild does not exist anymore!")
            return@Button
        }
        if (owner.getGuildJoinType() == GuildJoinType.REQUEST) {
            player.error("This guild is in 'REQUEST-only' mode.")
            player.error("Change it to 'ANY' or 'INVITE-only' mode to accept invites.")
            return@Button
        }
        val ownerLevel = owner.getGuildLevel()
        if (owner.getGuildMemberCount() >= ownerLevel * 5) {
            player.error("This guild has reached its current member cap!")
            return@Button
        }

        owner.addMemberToGuild(player)
        player.removeGuildQueueEntries(GuildJoinType.REQUEST)
        nav.back()
    },
    modifier = modifier
) {
    Text("<green>Accept INVITE".miniMsg(), modifier = Modifier.size(3, 3))
}

@Composable
fun GuildUIScope.DeclineGuildInviteButton(owner: OfflinePlayer, modifier: Modifier = Modifier) = Button(
    onClick = {
        player.removeGuildQueueEntries(GuildJoinType.INVITE)
        if (owner.hasGuild())
            player.info("<gold><b>❌ <yellow>You denied the invite from <gold><i>${owner.getGuildName()}")
        nav.back()
    }
) {
    Text("<red>Decline INVITE".miniMsg(), modifier = modifier.size(3, 3))
}
