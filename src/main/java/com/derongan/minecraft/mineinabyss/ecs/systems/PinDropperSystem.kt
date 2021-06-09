package com.derongan.minecraft.mineinabyss.ecs.systems

import com.derongan.minecraft.mineinabyss.ecs.components.DescentContext
import com.derongan.minecraft.mineinabyss.ecs.components.pins.PinDrop
import com.derongan.minecraft.mineinabyss.world.layer
import com.mineinabyss.geary.minecraft.access.geary
import com.mineinabyss.geary.minecraft.events.Events
import com.mineinabyss.geary.minecraft.store.encode
import com.mineinabyss.idofront.items.editItemMeta
import de.erethon.headlib.HeadLib
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class PinDropperSystem : Listener {
    @EventHandler
    fun EntityDeathEvent.dropPinOnDeath() {
        val player = entity.killer ?: return
        val gearyPlayer = geary(player)
        val layer = player.location.layer ?: return
        val descent = gearyPlayer.get<DescentContext>() ?: return

        if (layer.key !in descent.pinUsedLayers) {
            //TODO drop chance

            drops += HeadLib.PLAIN_RED.toItemStack("Abyssal Pin").editItemMeta {
                persistentDataContainer.apply {
                    encode(PinDrop(layer.key))
                    encode(
                        Events(
                            mapOf("rightClick" to listOf(AddPinAction()))
                        )
                    )
                }
            }
        }
    }
}
