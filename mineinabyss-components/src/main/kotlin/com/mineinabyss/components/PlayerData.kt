package com.mineinabyss.components

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import com.mineinabyss.geary.minecraft.access.toGeary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("mineinabyss:player_data")
@AutoscanComponent
class PlayerData(
    var isAffectedByCurse: Boolean = true,
    var curseAccrued: Double = 0.0,
    var exp: Double = 0.0,
    var keepInvStatus: Boolean = true,
    var showPvPMessage: Boolean = true,
    var pvpUndecided: Boolean = true,
    var pvpStatus: Boolean = false
) {
    val level: Int get() = exp.toInt() / 10 //TODO write a proper formula

    fun addExp(exp: Double) {
        this.exp += exp
    }
}

val Player.playerData get() = toGeary().getOrSetPersisting { PlayerData() }
