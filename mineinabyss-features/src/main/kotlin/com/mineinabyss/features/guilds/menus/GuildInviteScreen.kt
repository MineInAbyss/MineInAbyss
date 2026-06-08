package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mineinabyss.features.guilds.database.GuildJoinType
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.textcomponents.miniMsg
import me.dvyy.compose.mini.layout.modifiers.height
import me.dvyy.compose.mini.layout.modifiers.offset
import me.dvyy.compose.mini.layout.modifiers.size
import me.dvyy.compose.mini.modifier.Modifier
import org.bukkit.OfflinePlayer

@Composable
fun GuildUIScope.GuildInviteScreen(
    owner: OfflinePlayer,
    onBack: () -> Unit,
    onNavigateHome: () -> Unit,
) = Chest(":space_-8::guild_inbox_handle_menu:", Modifier.height(5.dp)) {
    GuildLabel(owner, Modifier.offset(4.dp, 0.dp))
    AcceptGuildInviteButton(owner, Modifier.offset(1.dp, 1.dp), onBack, onNavigateHome)
    DeclineGuildInviteButton(owner, Modifier.offset(5.dp, 1.dp), onBack, onNavigateHome)
    BackButton(Modifier.offset(4.dp, 4.dp))
}

@Composable
fun GuildLabel(owner: OfflinePlayer, modifier: Modifier) = Button {
    Item(
        TitleItem.head(
            owner, "<gold><b>Current Guild Info</b>".miniMsg(),
            "<yellow><b>Guild Name:</b> <i>${owner.getGuildName()}".miniMsg(),
            "<yellow><b>Guild Owner:</b> <i>${owner.name}".miniMsg(),
            "<yellow><b>Guild Level:</b> <i>${owner.getGuildLevel()}".miniMsg(),
            "<yellow><b>Guild Members:</b> <i>${owner.getGuildMemberCount()}".miniMsg(),
            isFlat = true,
            isLarge = true,
            isCenterOfInv = true
        ), modifier = modifier
    )
}

@Composable
fun GuildUIScope.AcceptGuildInviteButton(
    owner: OfflinePlayer,
    modifier: Modifier,
    onBack: () -> Unit,
    onNavigateHome: () -> Unit,
) = Button(
    modifier = modifier,
    onClick = {
        if (!owner.hasGuild()) {
            onNavigateHome()
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
        onBack()
    },
) {
    Text("<green>Accept INVITE".miniMsg(), modifier = Modifier.size(3.dp, 3.dp))
}

@Composable
fun GuildUIScope.DeclineGuildInviteButton(
    owner: OfflinePlayer,
    modifier: Modifier,
    onBack: () -> Unit,
    onNavigateHome: () -> Unit,
) = Button(
    modifier = modifier,
    onClick = {
        val guildName = owner.getGuildName() ?: ""
        guildName.removeGuildQueueEntries(player, GuildJoinType.INVITE)
        if (owner.hasGuild())
            player.info("<gold><b>❌</b> <yellow>You denied the invite from </yellow><i>$guildName")
        if (player.getNumberOfGuildRequests() > 1) onBack()
        else onNavigateHome()
    }
) {
    Text("<red>Decline INVITE".miniMsg(), modifier = Modifier.size(3.dp, 3.dp))
}
