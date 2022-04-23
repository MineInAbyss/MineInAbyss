package com.mineinabyss.pins

import com.mineinabyss.components.pins.ActivePins
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.api.annotations.Handler
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.api.systems.provideDelegate
import org.bukkit.entity.Player

class PinRegistrySystem: GearyListener() {
    val TargetScope.player by added<Player>()

    @Handler
    fun TargetScope.addActivePins() {
        entity.getOrSetPersisting { ActivePins() }
    }
}
