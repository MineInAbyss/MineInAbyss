package com.mineinabyss.pins

import com.mineinabyss.components.pins.PinDrop
import com.mineinabyss.geary.ecs.accessors.EventResultScope
import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.entities.parent
import com.mineinabyss.geary.ecs.events.handlers.ComponentAddHandler
import com.mineinabyss.geary.minecraft.events.ItemInteraction
import com.mineinabyss.looty.ecs.components.itemcontexts.PlayerInventoryContext

class AbyssalPinBehaviour : GearyListener() {
    private val ResultScope.pinDrop by get<PinDrop>()
    private val ResultScope.inventoryContext by get<PlayerInventoryContext>()

    private inner class RightClick : ComponentAddHandler() {
        val EventResultScope.hit by get<ItemInteraction>()

        override fun ResultScope.handle(event: EventResultScope) {
            if (event.hit.leftClick) {
                inventoryContext.removeItem()
                entity.removeEntity()
                entity.parent?.callEvent(ActivateAbyssalPinEvent(pinDrop))
            }
        }
    }
}
