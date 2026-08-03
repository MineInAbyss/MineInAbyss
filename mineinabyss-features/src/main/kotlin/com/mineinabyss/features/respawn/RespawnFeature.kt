package com.mineinabyss.features.respawn

import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.features.singleConfig
import com.mineinabyss.idofront.messaging.success

val RespawnFeature = module("respawn") {
    //TODO: test with blocky/Nexo
    require(get<AbyssFeatureConfig>().respawn.enabled) { "Respawn feature is disabled" }

    singleConfig<RespawnConfig>("respawn.yml")
    listeners(new(::RespawnListener))
}.mainCommand {
    "respawn" {
        "getLoc" {
            executes.asPlayer {
                player.success(player.getBonfireLocation())
            }
        }
    }
}