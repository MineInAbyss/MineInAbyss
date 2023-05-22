package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope
import org.bukkit.entity.Player

class GondolaTracker : GearyListener() {
    val TargetScope.player by onSet<Player>()

    @Handler
    fun TargetScope.setOnLogin() {
        entity.getOrSetPersisting { UnlockedGondolas() }
    }
}
