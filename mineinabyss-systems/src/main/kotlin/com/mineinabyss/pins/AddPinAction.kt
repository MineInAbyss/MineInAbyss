package com.mineinabyss.pins

import com.mineinabyss.components.DescentContext
import com.mineinabyss.components.pins.PinDrop
import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.entities.parent
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.looty.ecs.components.itemcontexts.PlayerInventoryContext
import com.mineinabyss.pins.ui.PinSelectionMenu
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
        val context = parent.get<DescentContext>() ?: return false
        val player = parent.get<Player>() ?: return false

        inventoryContext.removeItem()
        removeEntity()

        if (drop.layerKey in context.pinUsedLayers) return false

        context.pinUsedLayers += drop.layerKey
        guiy {
            PinSelectionMenu(player)
        }
        return true
    }
}
