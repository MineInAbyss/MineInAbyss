package com.mineinabyss.features.curse

import com.mineinabyss.components.editPlayerData
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.features.requirePlugins
import com.mineinabyss.idofront.messaging.success

val CurseFeature = module("curse") {
    require(get<AbyssFeatureConfig>().curse.enabled) { "Curse feature is disabled" }
    requirePlugins("DeeperWorld")

    listeners(CurseAscensionListener(), CurseEffectsListener())
}.mainCommand {
    "curse" {
        description = "Commands to toggle curse"

        executes.asPlayer().withPermission("mineinabyss.curse").args(
            "toggled" to Args.bool()
        ) { toggled ->
            player.editPlayerData { isAffectedByCurse = toggled }
            val enabled = if (toggled) "enabled" else "disabled"
            sender.success("Curse $enabled for ${player.name}")
        }
    }
}
