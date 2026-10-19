package com.mineinabyss.features.regioncontexts

import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.helpers.luckPerms
import com.mineinabyss.idofront.features.requirePlugins

val RegionContextsFeature = module("region-contexts") {
    require(get<AbyssFeatureConfig>().regionContexts.enabled) { "Region contexts feature is disabled" }
    requirePlugins("LuckPerms")
    val regions = requireNotNull(CartridgeRegionLookup.create()) { "Server is not running Cartridge" }

    val calculator = CartridgeRegionCalculator(regions)
    luckPerms.contextManager.registerCalculator(calculator)
    addCloseable { luckPerms.contextManager.unregisterCalculator(calculator) }
}
