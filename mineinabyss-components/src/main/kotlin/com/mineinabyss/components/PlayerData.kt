package com.mineinabyss.components

import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.*

@Serializable
@SerialName("mineinabyss:player_data")
class PlayerData(
    var isAffectedByCurse: Boolean = true,
    var curseAccrued: Double = 0.0,
    var exp: Double = 0.0,
    var keepInvStatus: Boolean = true,
    var showPvpPrompt: Boolean = true,
    var pvpUndecided: Boolean = true,
    var pvpStatus: Boolean = false,
    var orthCoinsHeld: Int = 0,
    var mittyTokensHeld: Int = 0,
    var showPlayerBalance: Boolean = true,
    var displayProfileArmor: Boolean = true,
    var recentInteractEntity: @Serializable(with = UUIDSerializer::class) UUID? = null
) {
    val level: Int get() = exp.toInt() / 10 //TODO write a proper formula

    fun addExp(exp: Double) {
        this.exp += exp
    }

    fun getRecentEntity() : Entity? {
        return recentInteractEntity?.let { Bukkit.getEntity(it) }
    }
}

val Player.playerData get() = toGeary().getOrSetPersisting { PlayerData() }
