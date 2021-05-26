package com.derongan.minecraft.mineinabyss.ecs.components

import com.derongan.minecraft.mineinabyss.world.Layer
import com.mineinabyss.geary.ecs.prefab.PrefabKey
import kotlinx.serialization.Serializable

@Serializable
class DescentContext {
    val acquiredPins = mutableMapOf<String, PrefabKey>()

    //TODO implement
    val lowestDepth: Int = 0
}
