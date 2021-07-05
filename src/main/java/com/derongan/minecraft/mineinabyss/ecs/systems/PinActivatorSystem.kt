package com.derongan.minecraft.mineinabyss.ecs.systems

import com.derongan.minecraft.mineinabyss.ecs.components.pins.ActivePins
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.engine.iteration.QueryResult
import com.mineinabyss.geary.ecs.entities.addParent
import com.mineinabyss.geary.ecs.entities.addPrefab
import com.mineinabyss.geary.ecs.prefab.PrefabKey

class PinActivatorSystem : TickingSystem() {
    private val QueryResult.pins by get<ActivePins>()

    override fun QueryResult.tick() {
        val activate: Set<PrefabKey> = pins - pins.loadedEntities.keys
        val inactive: Map<PrefabKey, GearyEntity> = pins.loadedEntities - pins
        pins.loadedEntities -= inactive.keys

        activate.forEach { key ->
            val prefab = key.toEntity() ?: return@forEach
            val entity = Engine.entity {
                addParent(entity)
                addPrefab(prefab)
            }
            pins.loadedEntities[key] = entity
        }
    }
}
