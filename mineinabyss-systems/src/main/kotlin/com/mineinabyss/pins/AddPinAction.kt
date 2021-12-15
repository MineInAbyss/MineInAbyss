package com.mineinabyss.pins

import com.mineinabyss.components.descent.DescentContext
import com.mineinabyss.components.pins.PinDrop
import com.mineinabyss.geary.ecs.accessors.EventResultScope
import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.events.handlers.ComponentAddHandler
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.pins.ui.AbyssalPinSelectionMenu
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

class ActivateAbyssalPinEvent(
    val dropInfo: PinDrop
)

@Serializable
@SerialName("mineinabyss:add_pin")
class AddPinBehaviour : GearyListener() {
    private val ResultScope.descentContext by get<DescentContext>()
    private val ResultScope.player by get<Player>()

    private inner class RightClick : ComponentAddHandler() {
        val EventResultScope.dropInfo by get<ActivateAbyssalPinEvent>().map { it.dropInfo }

        override fun ResultScope.handle(event: EventResultScope) {
            if (event.dropInfo.layerKey in descentContext.pinUsedLayers) return

            descentContext.pinUsedLayers += event.dropInfo.layerKey
            guiy {
                AbyssalPinSelectionMenu(player)
            }
        }
    }
}
