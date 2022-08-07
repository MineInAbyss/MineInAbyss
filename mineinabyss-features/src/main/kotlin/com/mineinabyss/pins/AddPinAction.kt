package com.mineinabyss.pins

import com.mineinabyss.components.descent.DescentContext
import com.mineinabyss.components.pins.PinDrop
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.accessors.building.map
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
    private val TargetScope.descentContext by onSet<DescentContext>()
    private val TargetScope.player by onSet<Player>()

    private val EventScope.dropInfo by get<ActivateAbyssalPinEvent>().map { it.dropInfo }

    @Handler
    fun TargetScope.onRightClick(event: EventScope) {
        if (event.dropInfo.layerKey in descentContext.pinUsedLayers) return

        descentContext.pinUsedLayers += event.dropInfo.layerKey
        guiy {
            AbyssalPinSelectionMenu(player)
        }
    }
}
