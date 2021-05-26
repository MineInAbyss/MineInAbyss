package com.derongan.minecraft.mineinabyss.ecs.systems

import com.derongan.minecraft.mineinabyss.ecs.components.ActivePins
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.entities.addParent
import com.mineinabyss.geary.ecs.entities.addPrefab

class PinActivatorSystem : TickingSystem() {
    private val pins by get<ActivePins>()

    override fun GearyEntity.tick() {
        val activate = pins.active - pins.loadedEntities.keys
        val inactive = pins.loadedEntities - pins.active
        pins.loadedEntities -= inactive.keys
        inactive.forEach { it.value.removeEntity() }

        activate.forEach { key ->
            val prefab = key.toEntity() ?: return@forEach
            val entity = Engine.entity {
                addParent(this@tick)
                addPrefab(prefab)
            }
            pins.loadedEntities[key] = entity
        }
    }
}
