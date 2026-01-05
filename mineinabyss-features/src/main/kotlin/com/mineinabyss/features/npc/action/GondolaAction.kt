package com.mineinabyss.features.npc.action

import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

class GondolaAction {

}
@Serializable
class GondolaUnlockAction(
    val id: String
) {
    fun unlock(player: Player) {

    }
}
