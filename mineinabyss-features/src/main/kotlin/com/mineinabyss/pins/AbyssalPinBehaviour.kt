package com.mineinabyss.pins

import com.mineinabyss.components.pins.PinDrop
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.papermc.bridge.components.LeftClicked
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class AbyssalPinBehaviour : GearyListener() {
    val TargetScope.pinDrop by onSet<PinDrop>()
    val TargetScope.item by onSet<ItemStack>()
    val EventScope.hit by onAdd<LeftClicked>()

    @Handler
    fun TargetScope.handleComponentAdd(event: EventScope) {
        item.type = Material.AIR
        entity.removeEntity()
        entity.parent?.callEvent(ActivateAbyssalPinEvent(pinDrop))
    }
}
