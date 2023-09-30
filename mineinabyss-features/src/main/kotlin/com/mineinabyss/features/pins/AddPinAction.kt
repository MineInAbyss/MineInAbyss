package com.mineinabyss.features.pins

import com.mineinabyss.components.descent.DescentContext
import com.mineinabyss.components.pins.PinDrop
import com.mineinabyss.features.pins.ui.AbyssalPinSelectionMenu
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.guiy.inventory.guiy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

class ActivateAbyssalPinEvent(
    val dropInfo: PinDrop
)

@Serializable
@SerialName("mineinabyss:add_pin")
class AddPinBehaviour : GearyListener() {
    private val Pointers.descentContext by get<DescentContext>().whenSetOnTarget()
    private val Pointers.player by get<Player>().whenSetOnTarget()

    private val Pointers.dropInfo by get<ActivateAbyssalPinEvent>().map { it.dropInfo }.on(event)

    override fun Pointers.handle() {
        if (dropInfo.layerKey in descentContext.pinUsedLayers) return

        descentContext.pinUsedLayers += dropInfo.layerKey
        guiy {
            AbyssalPinSelectionMenu(player)
        }
    }
}
