package com.mineinabyss.features.pins

import com.mineinabyss.components.descent.DescentContext
import com.mineinabyss.components.pins.PinDrop
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.features.helpers.layer
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack

class PinDropListener : Listener {
    @EventHandler
    fun EntityDeathEvent.dropPinOnDeath() {
        val player = entity.killer ?: return
        val gearyPlayer = player.toGeary()
        val layer = player.location.layer ?: return
        val descent = gearyPlayer.get<DescentContext>() ?: return

        if (layer.key !in descent.pinUsedLayers) {
            //TODO drop chance

            drops += ItemStack(Material.RED_WOOL).editItemMeta {
                displayName(Component.text("Abyssal Pin"))
                persistentDataContainer.encode(PinDrop(layer.key))
            }
        }
    }
}

