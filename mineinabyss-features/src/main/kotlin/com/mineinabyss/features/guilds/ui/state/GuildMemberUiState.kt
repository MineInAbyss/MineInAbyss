package com.mineinabyss.features.guilds.ui.state

import com.mineinabyss.features.guilds.data.tables.GuildRank
import java.util.UUID

data class GuildMemberUiState(
    val name: String,
    val uuid: UUID,
    val rank: GuildRank,
    val currentGuild: Int,
) {
    val isOwner = rank == GuildRank.OWNER
    val isCaptainOrAbove = rank <= GuildRank.CAPTAIN
}
