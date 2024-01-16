package com.mineinabyss.features.tools.depthmeter

import com.mineinabyss.components.tools.DepthMeter
import com.mineinabyss.components.tools.ShowDepthMeterHud
import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.idofront.time.ticks
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareGrindstoneEvent

class DepthHudSystem : RepeatingSystem(5.ticks) {
    private val Pointer.player by get<Player>()

    override fun Pointer.tick() {
        if (!player.isConnected) return
        val depthMeters = player.inventory.withIndex().filter { player.inventory.toGeary()?.get(it.index)?.has<DepthMeter>() == true }.mapNotNull { it.value }
        when {
            depthMeters.any { Enchantment.ARROW_INFINITE !in it.enchantments } ->
                player.toGeary().add<ShowDepthMeterHud>()
            else -> player.toGeary().remove<ShowDepthMeterHud>()
        }
    }
}

class DepthMeterBukkitListener : Listener {
    @EventHandler
    fun PrepareGrindstoneEvent.onGrindDepthMeter() {
        if (this.result?.itemMeta?.persistentDataContainer?.decodePrefabs()?.contains(PrefabKey.of("mineinabyss:depth_meter")) == true)
            result = null
    }
}
