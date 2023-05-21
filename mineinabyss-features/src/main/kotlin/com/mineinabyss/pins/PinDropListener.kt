package com.mineinabyss.pins

import com.mineinabyss.components.descent.DescentContext
import com.mineinabyss.components.pins.PinDrop
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.store.encode
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.mineinabyss.core.layer
import de.erethon.headlib.HeadLib
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class PinDropListener : Listener {
    @EventHandler
    fun EntityDeathEvent.dropPinOnDeath() {
        val player = entity.killer ?: return
        val gearyPlayer = player.toGeary()
        val layer = player.location.layer ?: return
        val descent = gearyPlayer.get<DescentContext>() ?: return

        if (layer.key !in descent.pinUsedLayers) {
            //TODO drop chance

            drops += HeadLib.PLAIN_RED.toItemStack("Abyssal Pin").editItemMeta {
                persistentDataContainer.apply {
                    encode(PinDrop(layer.key))
                }
            }
        }
    }
}

