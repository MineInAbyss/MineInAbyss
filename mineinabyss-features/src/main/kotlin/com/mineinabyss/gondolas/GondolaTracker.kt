package com.mineinabyss.gondolas

import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.api.annotations.Handler
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.api.systems.provideDelegate
import org.bukkit.entity.Player

class GondolaTracker : GearyListener() {
    val TargetScope.player by added<Player>()

    @Handler
    fun TargetScope.setOnLogin() {
        entity.getOrSetPersisting { UnlockedGondolas() }
    }
}
