package com.mineinabyss.features.tools

import com.mineinabyss.components.playerData
import com.mineinabyss.features.tools.depthmeter.ShowDepthSystem
import com.mineinabyss.features.tools.depthmeter.createDepthHudSystem
import com.mineinabyss.features.tools.depthmeter.createToggleDepthHudAction
import com.mineinabyss.features.tools.grapplinghook.GrapplingHookListener
import com.mineinabyss.features.tools.sickle.SickleListener
import com.mineinabyss.features.tools.sickle.createHarvestAction
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners

class ToolsFeature : Feature() {
    override val dependsOn: Set<String> = setOf("DeeperWorld")
    override fun FeatureDSL.enable() = geary.run {
        mainCommand {
            "replant" {
                playerAction {
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
                playerAction {
                    ShowDepthSystem.run {
                        player.sendDepthMessage()
                    }
                }
            }
        }
        tabCompletion {
            if (args.size == 1) listOf("replant", "depth").filter { it.startsWith(args[0]) }
            else emptyList()
        }
        ShowDepthSystem.register()
        createToggleDepthHudAction()
        createHarvestAction()
        createDepthHudSystem()

        plugin.listeners(
            SickleListener(),
            GrapplingHookListener(),
        )
    }
}
