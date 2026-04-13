package com.mineinabyss.features.tools

import com.mineinabyss.components.editPlayerData
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.abyss
import com.mineinabyss.features.tools.depthmeter.ShowDepthSystem
import com.mineinabyss.features.tools.depthmeter.createDepthHudSystem
import com.mineinabyss.features.tools.depthmeter.createToggleDepthHudAction
import com.mineinabyss.features.tools.grapplinghook.GrapplingHookListener
import com.mineinabyss.features.tools.sickle.SickleListener
import com.mineinabyss.features.tools.sickle.createHarvestAction
import com.mineinabyss.geary.papermc.gearyWorld
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.features.requirePlugins
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success

val ToolsFeature = module("tools") {
    require(get<AbyssFeatureConfig>().tools.enabled) { "Tools feature is disabled" }
    requirePlugins("DeeperWorld")
    listeners(
        SickleListener(),
        GrapplingHookListener(),
    )

    gearyWorld {
        ShowDepthSystem.register(abyss.gearyGlobal)
        createToggleDepthHudAction()
        createHarvestAction()
        createDepthHudSystem()
    }
}.mainCommand {
    "replant" {
        executes.asPlayer {
            player.editPlayerData {
                replant = !replant
                when {
                    replant -> {
                        player.success("Crops will now automatically be replanted!")
                        if (sender != player) sender.info("Crops will now automatically be replanted for ${player.name}!")
                    }

                    else -> {
                        player.error("Crops will not automatically be replanted!")
                        if (sender != player) sender.info("Crops will not automatically be replanted for ${player.name}!")
                    }
                }
            }
        }
    }
    "depth" {
        executes.asPlayer {
            ShowDepthSystem.run {
                player.sendDepthMessage()
            }
        }
    }
}
