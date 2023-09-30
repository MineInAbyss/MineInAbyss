package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import org.bukkit.entity.Player

@OptIn(UnsafeAccessors::class)
class GondolaTracker : GearyListener() {
    val Pointers.player by get<Player>().whenSetOnTarget()

    override fun Pointers.handle() {
        event.entity.getOrSetPersisting { UnlockedGondolas() }
    }
}
