package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.serialization.getOrSetPersisting
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query
import org.bukkit.entity.Player

fun GearyModule.createGondolaTracker() = observe<OnSet>()
    .exec(query<Player>()) { player ->
        entity.getOrSetPersisting<UnlockedGondolas> { UnlockedGondolas() }
    }
