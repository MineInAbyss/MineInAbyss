package com.mineinabyss.pins

import com.mineinabyss.components.pins.PinDrop
import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.api.systems.GearyHandlerScope
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.minecraft.access.toGeary
import com.mineinabyss.geary.minecraft.events.onItemRightClick
import com.mineinabyss.looty.ecs.components.itemcontexts.PlayerInventoryContext

object AbyssalPinBehaviour : GearyListener() {
    private val ResultScope.pinDrop by get<PinDrop>()
    private val ResultScope.inventoryContext by get<PlayerInventoryContext>()

    override fun GearyHandlerScope.register() {
        onItemRightClick { event ->
            inventoryContext.removeItem()
            entity.removeEntity()

            event.player.toGeary().callEvent(ActivateAbyssalPinEvent(pinDrop))
        }
    }
}
