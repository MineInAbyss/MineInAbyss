package com.mineinabyss.features.relics

import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareGrindstoneEvent

class StarCompassBukkitListener : Listener {
    @EventHandler
    fun PrepareGrindstoneEvent.onGrindStarCompass() {
        if (result?.itemMeta
                ?.persistentDataContainer
                ?.decodePrefabs()
                ?.contains(PrefabKey.of("mineinabyss:star_compass")) != true
        ) return

        result = null
    }
}
