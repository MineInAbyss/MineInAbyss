package com.mineinabyss.features.tools

import com.mineinabyss.features.tools.depthmeter.DepthHudSystem
import com.mineinabyss.features.tools.depthmeter.DepthMeterBukkitListener
import com.mineinabyss.features.tools.depthmeter.ShowDepthSystem
import com.mineinabyss.features.tools.depthmeter.ToggleDepthHudSystem
import com.mineinabyss.features.tools.grapplinghook.GrapplingHookListener
import com.mineinabyss.features.tools.sickle.HarvestListener
import com.mineinabyss.features.tools.sickle.SickleListener
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.plugin.listeners

class ToolsFeature : Feature() {
    override val dependsOn: Set<String> = setOf("DeeperWorld")
    override fun FeatureDSL.enable() {
        mainCommand {
            "depth" {
                playerAction {
                    ShowDepthSystem().run {
                        player.sendDepthMessage()
                    }
                }
            }
        }
        geary.pipeline.addSystems(
            ShowDepthSystem(),
            ToggleDepthHudSystem(),
            DepthHudSystem(),
            HarvestListener(),
        )
        plugin.listeners(
            SickleListener(),
            GrapplingHookListener(),
            DepthMeterBukkitListener(),
        )
    }
}
