package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.features.guilds.database.GuildJoinType
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.items.PlayerHead
import com.mineinabyss.guiy.components.items.PlayerHeadType
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.OfflinePlayer

@Composable
fun GuildViewModel.GuildInviteScreen(owner: OfflinePlayer) {
    GuildLabel(owner, Modifier.at(4, 0))
    AcceptGuildInviteButton(owner, Modifier.at(1, 1))
    DeclineGuildInviteButton(owner, Modifier.at(5, 1))
    BackButton(Modifier.at(4, 4))
}

@Composable
fun GuildLabel(owner: OfflinePlayer, modifier: Modifier) = Button {
    PlayerHead(
        owner,
        "<gold><b>Current Guild Info</b>",
        "<yellow><b>Guild Name:</b> <i>${owner.getGuildName()}",
        "<yellow><b>Guild Owner:</b> <i>${owner.name}",
        "<yellow><b>Guild Level:</b> <i>${owner.getGuildLevel()}",
        "<yellow><b>Guild Members:</b> <i>${owner.getGuildMemberCount()}",
        type = PlayerHeadType.LARGE_CENTER,
        modifier = modifier
    )
}

@Composable
fun GuildViewModel.AcceptGuildInviteButton(owner: OfflinePlayer, modifier: Modifier) = Button(
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

        if (!owner.addMemberToGuild(player)) return@Button player.error("Failed to join ${guildName}.")
        player.removeGuildQueueEntries(GuildJoinType.REQUEST)
        nav.back()
    },
) {
    Text("<green>Accept INVITE", modifier = Modifier.size(3, 3))
}

@Composable
fun GuildViewModel.DeclineGuildInviteButton(owner: OfflinePlayer, modifier: Modifier) = Button(
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
    Text("<red>Decline INVITE", modifier = Modifier.size(3, 3))
}
