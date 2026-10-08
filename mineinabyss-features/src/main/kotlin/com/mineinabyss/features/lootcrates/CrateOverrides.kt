package com.mineinabyss.features.lootcrates

import com.mineinabyss.features.abyss
import com.nexomc.nexo.api.NexoFurniture

class CrateOverrides(lootCrates: LootCrates) {
    private val override = LootCrateItemOverride(lootCrates)
    private var covered = emptySet<String>()
    private var closed = false

    @Synchronized
    fun cover(itemIDs: Set<String>) {
        if (closed || covered.containsAll(itemIDs)) return
        covered += itemIDs
        NexoFurniture.unregisterItemOverrides(abyss)
        NexoFurniture.registerItemOverride(abyss, override, covered)
    }

    fun cover(itemID: String) = cover(setOf(itemID))

    @Synchronized
    fun close() {
        closed = true
        NexoFurniture.unregisterItemOverrides(abyss)
    }
}
