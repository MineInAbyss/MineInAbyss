package com.mineinabyss.features.tools.depthmeter

import com.mineinabyss.components.tools.ShowDepthMeterHud
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.idofront.time.ticks
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareGrindstoneEvent

class DepthHudSystem : RepeatingSystem(5.ticks) {
    private val Pointer.hudShown by get<ShowDepthMeterHud>()

    private val hudEnabledQuery = object : GearyQuery() {
        val Pointer.player by get<Player>()
        val Pointer.hudShown by get<ShowDepthMeterHud>()
    }

    // TODO refactor for code reuse
    @OptIn(UnsafeAccessors::class)
    override fun tickAll() {
        val oldPlayersWithHud = hudEnabledQuery.matchedEntities.toSet()
        val newPlayersWithHud = mutableSetOf<GearyEntity>()
        forEach {
            val player = it.entity.parent ?: return@forEach
            newPlayersWithHud += player
        }

        // Update component on players that need an update
        oldPlayersWithHud.minus(newPlayersWithHud).forEach {
            it.remove<ShowDepthMeterHud>()
        }
        newPlayersWithHud.minus(oldPlayersWithHud).forEach {
            it.set(ShowDepthMeterHud())
        }
    }
}

class DepthMeterBukkitListener : Listener {
    @EventHandler
    fun PrepareGrindstoneEvent.onGrindDepthMeter() {
        if (this.result?.itemMeta?.persistentDataContainer?.decodePrefabs()
                ?.contains(PrefabKey.of("mineinabyss:depth_meter")) == true
        )
            result = null
    }
}
