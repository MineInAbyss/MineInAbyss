package com.mineinabyss.features.npc.NpcAction

import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.gondolas.Ticket
import com.mineinabyss.features.gondolas.pass.TicketConfigHolder
import com.mineinabyss.features.gondolas.pass.isRouteUnlocked
import com.mineinabyss.features.gondolas.pass.unlockRoute
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
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
