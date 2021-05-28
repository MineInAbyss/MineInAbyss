package com.derongan.minecraft.mineinabyss.ecs.components

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.prefab.PrefabKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:active_pins")
@AutoscanComponent
class ActivePins(
    val active: MutableSet<PrefabKey> = mutableSetOf()
) {
    internal val loadedEntities = mutableMapOf<PrefabKey, GearyEntity>()
}
