package com.mineinabyss.features.guilds.menus

import com.mineinabyss.features.guilds.data.GuildRepository
import com.mineinabyss.features.guilds.database.GuildRank
import com.mineinabyss.features.guilds.database.entities.GuildPlayerEntity
import com.mineinabyss.features.guilds.extensions.GuildJoin
import com.mineinabyss.features.guilds.extensions.GuildMember
import com.mineinabyss.features.guilds.extensions.getGuildBalance
import com.mineinabyss.features.guilds.extensions.getGuildLevel
import com.mineinabyss.features.guilds.extensions.getGuildMemberCount
import com.mineinabyss.features.guilds.extensions.getGuildName
import com.mineinabyss.features.guilds.extensions.getGuildOwner
import com.mineinabyss.features.guilds.extensions.toOfflinePlayer
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.inventory.GuiyViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import java.util.UUID

data class GuildUiState(
    val name: String,
    val owner: GuildMemberUiState,
    val level: Int,
    val memberCount: Int,
    val balance: Int,
)

data class GuildMemberUiState(
    val name: String,
    val uuid: UUID,
    val rank: GuildRank,
)

class GuildViewModel(
    val player: Player,
    val openedFromHQ: Boolean,
    private val owner: GuiyOwner,
    private val repository: GuildRepository,
): GuiyViewModel() {
    val guildUiState = MutableStateFlow<GuildUiState?>(null)
    val memberUiState = MutableStateFlow<GuildMemberUiState?>(null)

    init {
        viewModelScope.launch {
            memberUiState.emit(repository.member(player.uniqueId))
        }
    }

    val guildName get() = player.getGuildName()
    val guildLevel get() = player.getGuildLevel()
    val nav = GuildNav { GuildScreen.Default(player) }
}
