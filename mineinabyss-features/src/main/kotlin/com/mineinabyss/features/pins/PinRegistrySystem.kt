package com.mineinabyss.features.pins

import com.mineinabyss.components.pins.ActivePins
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import org.bukkit.entity.Player

@OptIn(UnsafeAccessors::class)
class PinRegistrySystem: GearyListener() {
    val Pointers.player by get<Player>().whenSetOnTarget()

    override fun Pointers.handle() {
        event.entity.getOrSetPersisting { ActivePins() }
    }
}
