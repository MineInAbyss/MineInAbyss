package com.mineinabyss.pins

import com.mineinabyss.components.descent.DescentContext
import com.mineinabyss.components.pins.PinDrop
import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.api.systems.GearyHandlerScope
import com.mineinabyss.geary.ecs.api.systems.GearyListener
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
object AddPinBehaviour : GearyListener() {
    private val ResultScope.descentContext by get<DescentContext>()
    private val ResultScope.player by get<Player>()

    override fun GearyHandlerScope.register() {
        on<ActivateAbyssalPinEvent> { event ->
            if (event.dropInfo.layerKey in descentContext.pinUsedLayers) return@on

            descentContext.pinUsedLayers += event.dropInfo.layerKey
            guiy {
                AbyssalPinSelectionMenu(player)
            }
        }
    }
}
