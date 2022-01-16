package com.mineinabyss.pins

import com.mineinabyss.components.pins.PinDrop
import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.building.get
import com.mineinabyss.geary.ecs.api.autoscan.Handler
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.entities.parent
import com.mineinabyss.geary.minecraft.events.bridge.components.Interacted
import com.mineinabyss.looty.ecs.components.itemcontexts.PlayerInventoryContext

class AbyssalPinBehaviour : GearyListener() {
    val TargetScope.pinDrop by get<PinDrop>()
    val TargetScope.inventoryContext by get<PlayerInventoryContext>()
    val EventScope.hit by get<Interacted>()

    init {
        allAdded()
    }

    @Handler
    fun TargetScope.handleComponentAdd(event: EventScope) {
        if (event.hit.leftClick) {
            inventoryContext.removeItem()
            entity.removeEntity()
            entity.parent?.callEvent(ActivateAbyssalPinEvent(pinDrop))
        }
    }
}
