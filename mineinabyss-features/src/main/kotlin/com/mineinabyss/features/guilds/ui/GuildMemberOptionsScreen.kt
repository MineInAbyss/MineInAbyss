package com.mineinabyss.features.guilds.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.mineinabyss.features.guilds.data.tables.GuildRank
import com.mineinabyss.features.guilds.extensions.kickPlayerFromGuild
import com.mineinabyss.features.guilds.extensions.setGuildRank
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.OfflinePlayer

@Composable
fun GuildMemberOptionsScreen(member: OfflinePlayer) {
    Row(modifier = Modifier.at(0, 1)) {
        ChangeMemberRankButton(member, GuildRank.STEWARD, Modifier.size(2, 2))
        Spacer(1)
        ChangeMemberRankButton(member, GuildRank.CAPTAIN, Modifier.size(3, 2))
        Spacer(1)
        ChangeMemberRankButton(member, GuildRank.MEMBER, Modifier.size(2, 2))
    }

    KickGuildMemberButton(member, Modifier.at(4, 4))
    BackButton(Modifier.at(0, 4))
}

@Composable
fun ChangeMemberRankButton(member: OfflinePlayer, rank: GuildRank, modifier: Modifier = Modifier) {
    val isCaptain = isCaptainOrAbove.collectAsState().value
    Button(
        modifier = modifier,
        enabled = isCaptain,
        onClick = {
            player.setGuildRank(member, rank)
//            nav.back() // TODO navigation
        }
    ) {
        Text("<dark_aqua>Change GuildRank to <blue>$rank".miniMsg(), modifier = modifier)
    }
}

@Composable
fun KickGuildMemberButton(member: OfflinePlayer, modifier: Modifier = Modifier) =
    Button(
        modifier = modifier,
        onClick = {
            player.kickPlayerFromGuild(member)
//            nav.back() // TODO navigation
        }
    ) {
        Text("<red><i>Kick Member".miniMsg())
    }


