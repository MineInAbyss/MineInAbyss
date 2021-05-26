package com.derongan.minecraft.mineinabyss.ecs.components

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.prefab.PrefabKey
import kotlinx.serialization.Serializable

@Serializable
class ActivePins(
    val active: MutableSet<PrefabKey> = mutableSetOf()
) {
    internal val loadedEntities = mutableMapOf<PrefabKey, GearyEntity>()
}
