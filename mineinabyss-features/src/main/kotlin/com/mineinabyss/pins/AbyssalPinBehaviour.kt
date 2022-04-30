package com.mineinabyss.pins

import com.mineinabyss.components.pins.PinDrop
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.commons.components.interaction.Interacted
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope
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
