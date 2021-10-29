package com.mineinabyss.components.pins

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.prefab.PrefabKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("mineinabyss:active_pins")
@AutoscanComponent
class ActivePins(
    private val active: MutableSet<PrefabKey> = mutableSetOf()
) : MutableSet<PrefabKey> by active {
    @Transient
    val loadedEntities = mutableMapOf<PrefabKey, GearyEntity>()

    override fun remove(element: PrefabKey): Boolean {
        active.remove(element)
        return loadedEntities.remove(element)?.removeEntity() != null
    }

    //TODO when component removal events are added, move this behaviour there
    override fun clear() {
        active.clear()
        loadedEntities.values.forEach { it.removeEntity() }
        loadedEntities.clear()
    }
}
