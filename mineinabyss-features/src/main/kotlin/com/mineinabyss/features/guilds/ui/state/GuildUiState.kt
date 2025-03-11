package com.mineinabyss.features.guilds.ui.state

import com.mineinabyss.features.guilds.data.tables.GuildJoinType

data class GuildUiState(
    val id: Int,
    val name: String,
    val owner: GuildMemberUiState,
    val level: Int,
    val memberCount: Int,
    val members: List<GuildMemberUiState>,
    val balance: Int,
    val joinType: GuildJoinType,
) {
    val canAcceptNewMembers = memberCount < level * 5
}
