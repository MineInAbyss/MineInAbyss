package com.mineinabyss.pins

import com.mineinabyss.components.pins.ActivePins
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope
import org.bukkit.entity.Player

class PinRegistrySystem: GearyListener() {
    val TargetScope.player by added<Player>()

    @Handler
    fun TargetScope.addActivePins() {
        entity.getOrSetPersisting { ActivePins() }
    }
}
