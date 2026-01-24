package com.mineinabyss.features.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.features.guilds.database.GuildRank
import com.mineinabyss.features.guilds.extensions.isCaptainOrAbove
import com.mineinabyss.features.guilds.extensions.kickPlayerFromGuild
import com.mineinabyss.features.guilds.extensions.setGuildRank
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.OfflinePlayer

@Composable
fun GuildUIScope.GuildMemberOptionsScreen(
    member: OfflinePlayer,
    onBack: () -> Unit,
) = Chest(":space_-8::guild_member_action_menu:", Modifier.height(5)) {
    Row(modifier = Modifier.at(0, 1)) {
        ChangeMemberRankButton(member, GuildRank.STEWARD, Modifier.size(2, 2), onBack)
        Spacer(1)
        ChangeMemberRankButton(member, GuildRank.CAPTAIN, Modifier.size(3, 2), onBack)
        Spacer(1)
        ChangeMemberRankButton(member, GuildRank.MEMBER, Modifier.size(2, 2), onBack)
    }

    KickGuildMemberButton(member, Modifier.at(4, 4), onBack)
    BackButton(Modifier.at(0, 4))
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


