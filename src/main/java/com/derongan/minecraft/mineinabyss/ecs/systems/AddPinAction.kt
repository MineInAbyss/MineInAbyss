package com.derongan.minecraft.mineinabyss.ecs.systems

import com.derongan.minecraft.mineinabyss.ecs.components.ActivePins
import com.derongan.minecraft.mineinabyss.ecs.components.DescentContext
import com.derongan.minecraft.mineinabyss.ecs.components.PinDrop
import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.entities.parent
import com.mineinabyss.geary.ecs.prefab.PrefabManager
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.looty.ecs.components.PlayerInventoryContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:add_pin")
class AddPinAction : GearyAction() {
    private val GearyEntity.drop by get<PinDrop>()
    private val GearyEntity.inventoryContext by get<PlayerInventoryContext>()

    override fun GearyEntity.run(): Boolean {
        val parent = parent ?: return false
        val activePins = parent.get<ActivePins>() ?: return false
        val context = parent.get<DescentContext>() ?: return false
//        if (drop.layerName in context.acquiredPins) return false

        val prefab = PrefabManager.getPrefabsFor("looty").random()
        activePins.active.add(prefab)
        context.acquiredPins[drop.layerName] = prefab
        broadcast("Chose $prefab")

        inventoryContext.removeItem()
        removeEntity()
        return true
    }
}
