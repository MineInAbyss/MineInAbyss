package com.mineinabyss.features.pins

import com.mineinabyss.components.pins.PinDrop
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.papermc.bridge.components.LeftClicked
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

@OptIn(UnsafeAccessors::class)
class AbyssalPinBehaviour : GearyListener() {
    val Pointers.pinDrop by get<PinDrop>().whenSetOnTarget()
    val Pointers.item by get<ItemStack>().whenSetOnTarget()
    val Pointers.hit by get<LeftClicked>().whenAddedOnTarget()

    override fun Pointers.handle() {
        item.type = Material.AIR
        event.entity.removeEntity()
        event.entity.parent?.callEvent(ActivateAbyssalPinEvent(pinDrop))
    }
}
