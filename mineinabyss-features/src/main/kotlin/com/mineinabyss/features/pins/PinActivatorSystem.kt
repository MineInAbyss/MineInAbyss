package com.mineinabyss.features.pins

import com.mineinabyss.components.pins.ActivePins
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.helpers.addPrefab
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.Pointer

@OptIn(UnsafeAccessors::class)
class PinActivatorSystem : RepeatingSystem() {
    private val Pointer.pins by get<ActivePins>()

    override fun Pointer.tick() {
        val activate: Set<PrefabKey> = pins - pins.loadedEntities.keys
        val inactive: Map<PrefabKey, GearyEntity> = pins.loadedEntities - pins
        pins.loadedEntities -= inactive.keys

        activate.forEach { key ->
            val prefab = key.toEntityOrNull() ?: return@forEach
            val entity = entity {
                addParent(entity)
                addPrefab(prefab)
            }
            pins.loadedEntities[key] = entity
        }
    }
}
