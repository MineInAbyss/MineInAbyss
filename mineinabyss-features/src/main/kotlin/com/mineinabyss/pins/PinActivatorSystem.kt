package com.mineinabyss.pins

import com.mineinabyss.components.pins.ActivePins
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.helpers.addPrefab
import com.mineinabyss.geary.systems.TickingSystem
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.accessors.get

class PinActivatorSystem : TickingSystem() {
    private val TargetScope.pins by get<ActivePins>()

    override fun TargetScope.tick() {
        val activate: Set<PrefabKey> = pins - pins.loadedEntities.keys
        val inactive: Map<PrefabKey, GearyEntity> = pins.loadedEntities - pins
        pins.loadedEntities -= inactive.keys

        activate.forEach { key ->
            val prefab = key.toEntity() ?: return@forEach
            val entity = entity {
                addParent(entity)
                addPrefab(prefab)
            }
            pins.loadedEntities[key] = entity
        }
    }
}
