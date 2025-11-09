package com.mineinabyss.features.tools

import com.mineinabyss.components.playerData
import com.mineinabyss.features.AbyssContext
import com.mineinabyss.features.tools.depthmeter.ShowDepthSystem
import com.mineinabyss.features.tools.depthmeter.createDepthHudSystem
import com.mineinabyss.features.tools.depthmeter.createToggleDepthHudAction
import com.mineinabyss.features.tools.grapplinghook.GrapplingHookListener
import com.mineinabyss.features.tools.sickle.SickleListener
import com.mineinabyss.features.tools.sickle.createHarvestAction
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import org.koin.core.module.dsl.scopedOf

val ToolsFeature = feature("tools") {
    dependsOn {
        plugins("DeeperWorld")
    }

    scopedModule {
        scopedOf(::SickleListener)
        scopedOf(::GrapplingHookListener)
        scopedOf(::ShowDepthSystem)
    }

    onEnable {
        //TODO geary system hot reloading
        get<ShowDepthSystem>().register()
        get<AbyssContext>().gearyGlobal.run {
            createToggleDepthHudAction()
            createHarvestAction()
            createDepthHudSystem()
        }

        listeners(
            get<SickleListener>(),
            get<GrapplingHookListener>(),
        )
    }

    mainCommand {
        "replant" {
            playerExecutes {
                player.playerData.replant = !player.playerData.replant
                when {
                    player.playerData.replant -> {
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
        "depth" {
            playerExecutes {
                get<ShowDepthSystem>().run {
                    player.sendDepthMessage()
                }
            }
        }
    }
}
