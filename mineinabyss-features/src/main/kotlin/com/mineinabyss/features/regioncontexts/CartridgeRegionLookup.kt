package com.mineinabyss.features.regioncontexts

import com.mineinabyss.cartridge.region.Regions
import org.bukkit.Location

class CartridgeRegionLookup(private val regions: Regions) {

    fun namesAt(location: Location): Set<String> = buildSet {
        for (region in regions.regionsAt(location)) {
            generateSequence(region) { it.parent() }.forEach { add(it.name()) }
        }
        remove(GLOBAL_REGION)
    }

    companion object {
        private const val GLOBAL_REGION = "global"

        fun create(): CartridgeRegionLookup? = runCatching { CartridgeRegionLookup(Regions.regions()) }.getOrNull()
    }
}
