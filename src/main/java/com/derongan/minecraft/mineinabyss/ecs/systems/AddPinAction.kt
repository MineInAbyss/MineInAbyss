package com.derongan.minecraft.mineinabyss.ecs.systems

import com.derongan.minecraft.mineinabyss.ecs.components.PinDrop
import com.derongan.minecraft.mineinabyss.gui.PinSelectionMenu
import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.entities.parent
import com.mineinabyss.looty.ecs.components.PlayerInventoryContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("mineinabyss:add_pin")
class AddPinAction : GearyAction() {
    private val GearyEntity.drop by get<PinDrop>()
    private val GearyEntity.inventoryContext by get<PlayerInventoryContext>()

    override fun GearyEntity.run(): Boolean {
        val parent = parent ?: return false
//        val activePins = parent.get<ActivePins>() ?: return false
//        val context = parent.get<DescentContext>() ?: return false
        val player = parent.get<Player>() ?: return false
//        if (drop.layerName in context.acquiredPins) return false

        PinSelectionMenu(player).show(player)
//        val prefab = PrefabManager.getPrefabsFor("looty").random()
//        activePins.active.add(prefab)
//        context.acquiredPins[drop.layerName] = prefab

        inventoryContext.removeItem()
        removeEntity()
        return true
    }
}
