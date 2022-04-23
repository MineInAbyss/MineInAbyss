package com.mineinabyss.pins

import com.mineinabyss.components.pins.PinDrop
import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.api.annotations.Handler
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.api.systems.provideDelegate
import com.mineinabyss.geary.ecs.entities.parent
import com.mineinabyss.geary.papermc.events.bridge.components.Interacted
import com.mineinabyss.looty.ecs.components.itemcontexts.PlayerInventorySlotContext

class AbyssalPinBehaviour : GearyListener() {
    val TargetScope.pinDrop by added<PinDrop>()
    val TargetScope.inventoryContext by added<PlayerInventorySlotContext>()
    val EventScope.hit by added<Interacted>()

    @Handler
    fun TargetScope.handleComponentAdd(event: EventScope) {
        if (event.hit.leftClick) {
            inventoryContext.removeItem()
            entity.removeEntity()
            entity.parent?.callEvent(ActivateAbyssalPinEvent(pinDrop))
        }
    }
}
