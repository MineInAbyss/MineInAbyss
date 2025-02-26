package com.mineinabyss.features.helpers.ui

import com.mineinabyss.components.PlayerData
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.guiy.inventory.GuiyViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.bukkit.entity.Player

class PlayerDataViewModel(
    player: Player,
) : GuiyViewModel() {
    val playerData: StateFlow<PlayerData?> = player.toGeary().getAsFlow<PlayerData>().stateIn(
        viewModelScope,
        started = WhileSubscribed,
        initialValue = null
    )
}
