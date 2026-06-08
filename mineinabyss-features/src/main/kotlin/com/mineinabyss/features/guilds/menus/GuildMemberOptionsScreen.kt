package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mineinabyss.features.guilds.database.GuildRank
import com.mineinabyss.features.guilds.extensions.isCaptainOrAbove
import com.mineinabyss.features.guilds.extensions.kickPlayerFromGuild
import com.mineinabyss.features.guilds.extensions.setGuildRank
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.idofront.textcomponents.miniMsg
import me.dvyy.compose.mini.layout.jetpack.Row
import me.dvyy.compose.mini.layout.modifiers.height
import me.dvyy.compose.mini.layout.modifiers.offset
import me.dvyy.compose.mini.layout.modifiers.size
import me.dvyy.compose.mini.layout.modifiers.width
import me.dvyy.compose.mini.modifier.Modifier
import org.bukkit.OfflinePlayer

@Composable
fun GuildUIScope.GuildMemberOptionsScreen(
    member: OfflinePlayer,
    onBack: () -> Unit,
) = Chest(":space_-8::guild_member_action_menu:", Modifier.height(5.dp)) {
    Row(modifier = Modifier.offset(0.dp, 1.dp)) {
        ChangeMemberRankButton(member, GuildRank.STEWARD, Modifier.size(2.dp, 2.dp), onBack)
        Spacer(Modifier.width(1.dp))
        ChangeMemberRankButton(member, GuildRank.CAPTAIN, Modifier.size(3.dp, 2.dp), onBack)
        Spacer(Modifier.width(1.dp))
        ChangeMemberRankButton(member, GuildRank.MEMBER, Modifier.size(2.dp, 2.dp), onBack)
    }

    KickGuildMemberButton(member, Modifier.offset(4.dp, 4.dp), onBack)
    BackButton(Modifier.offset(0.dp, 4.dp))
}

@Composable
fun GuildUIScope.ChangeMemberRankButton(
    member: OfflinePlayer, rank: GuildRank, modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    Button(modifier, player.isCaptainOrAbove(), {
        player.setGuildRank(member, rank)
        onBack()
    }) {
        Text("<dark_aqua>Change GuildRank to <blue>$rank".miniMsg(), modifier = modifier)
    }
}

@Composable
fun GuildUIScope.KickGuildMemberButton(
    member: OfflinePlayer, modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    Button(modifier, onClick = {
        player.kickPlayerFromGuild(member)
        onBack()
    }) {
        Text("<red><i>Kick Member".miniMsg())
    }
}
