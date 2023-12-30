package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.features.guilds.database.GuildJoinType
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.head
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.textcomponents.miniMsg
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
        "<yellow><b>Guild Name:</b> <i>${owner.getGuildName()}".miniMsg(),
        "<yellow><b>Guild Owner:</b> <i>${owner.name}".miniMsg(),
        "<yellow><b>Guild Level:</b> <i>${owner.getGuildLevel()}".miniMsg(),
        "<yellow><b>Guild Members:</b> <i>${owner.getGuildMemberCount()}".miniMsg(),
        isCenterOfInv = true, isLarge = true
    ), modifier = modifier)
}

@Composable
fun GuildUIScope.AcceptGuildInviteButton(owner: OfflinePlayer, modifier: Modifier) = Button(
    modifier = modifier,
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
) {
    Text("<green>Accept INVITE".miniMsg(), modifier = Modifier.size(3, 3))
}

@Composable
fun GuildUIScope.DeclineGuildInviteButton(owner: OfflinePlayer, modifier: Modifier) = Button(
    modifier = modifier,
    onClick = {
        val guildName = owner.getGuildName() ?: ""
        guildName.removeGuildQueueEntries(player, GuildJoinType.INVITE)
        if (owner.hasGuild())
            player.info("<gold><b>❌</b> <yellow>You denied the invite from </yellow><i>$guildName")
        if (player.getNumberOfGuildRequests() > 1) nav.back()
        else nav.reset()
    }
) {
    Text("<red>Decline INVITE".miniMsg(), modifier = Modifier.size(3, 3))
}
