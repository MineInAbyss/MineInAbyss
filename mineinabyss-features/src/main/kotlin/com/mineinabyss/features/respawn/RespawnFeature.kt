package com.mineinabyss.features.respawn

import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.singleConfig

val RespawnFeature = module("respawn") {
    require(get<AbyssFeatureConfig>().respawn.enabled) { "Respawn feature is disabled" }

    singleConfig<RespawnConfig>("respawn.yml")
    listeners(new(::RespawnListener))
}
