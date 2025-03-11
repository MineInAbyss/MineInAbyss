package com.mineinabyss.features.guilds.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.mineinabyss.features.guilds.data.tables.GuildRank
import com.mineinabyss.features.guilds.ui.BackButton
import com.mineinabyss.features.guilds.ui.GuildViewModel
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.viewmodel.viewModel
import com.mineinabyss.idofront.textcomponents.miniMsg
import java.util.*

@Composable
fun GuildMemberOptionsScreen(
    member: UUID,
    navigateBack: () -> Unit,
    guildViewModel: GuildViewModel = viewModel(),
) = Chest(":space_-8::guild_member_action_menu:", Modifier.height(5)) {
    Row(modifier = Modifier.at(0, 1)) {
        ChangeMemberRankButton(member, GuildRank.STEWARD, Modifier.size(2, 2))
        Spacer(1)
        ChangeMemberRankButton(member, GuildRank.CAPTAIN, Modifier.size(3, 2))
        Spacer(1)
        ChangeMemberRankButton(member, GuildRank.MEMBER, Modifier.size(2, 2))
    }

    Button(
        onClick = { guildViewModel.kickMember(member); navigateBack() },
        modifier = Modifier.at(4, 4),
    ) {
        Text("<red><i>Kick Member".miniMsg())
    }
    BackButton(Modifier.at(0, 4))
}

@Composable
fun ChangeMemberRankButton(
    member: UUID,
    rank: GuildRank,
    modifier: Modifier = Modifier,
    guildViewModel: GuildViewModel = viewModel(),
) {
    val isCaptain = guildViewModel.memberInfo.collectAsState().value?.isCaptainOrAbove ?: return
    Button(
        modifier = modifier,
        enabled = isCaptain,
        onClick = { guildViewModel.setRank(member, rank) }
    ) {
        Text("<dark_aqua>Change GuildRank to <blue>$rank".miniMsg(), modifier = modifier)
    }
}
