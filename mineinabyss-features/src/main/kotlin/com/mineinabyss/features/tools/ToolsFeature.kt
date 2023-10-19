package com.mineinabyss.features.tools

import com.mineinabyss.features.relics.ToggleStarCompassHud
import com.mineinabyss.features.relics.ToggleStarCompassHudSystem
import com.mineinabyss.features.tools.depthmeter.DepthHudSystem
import com.mineinabyss.features.tools.depthmeter.ShowDepthSystem
import com.mineinabyss.features.tools.depthmeter.ToggleDepthHudSystem
import com.mineinabyss.features.tools.grapplinghook.GearyHookListener
import com.mineinabyss.features.tools.grapplinghook.GrapplingHookListener
import com.mineinabyss.features.tools.sickle.HarvestListener
import com.mineinabyss.features.tools.sickle.SickleListener
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ToolsFeature : AbyssFeature {
    override val dependsOn: Set<String> = setOf("DeeperWorld")
    override fun MineInAbyssPlugin.enableFeature() {
        commands {
            mineinabyss {
                "depth" {
                    playerAction {
                        ShowDepthSystem().run {
                            player.sendDepthMessage()
                        }
                    }
                }
            }
        }
        geary.pipeline.addSystems(
            ShowDepthSystem(),
            ToggleDepthHudSystem(),
            DepthHudSystem(),
            HarvestListener(),
            GearyHookListener()
        )
        listeners(SickleListener(), GrapplingHookListener())
    }
}
