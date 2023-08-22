package com.mineinabyss.features.relics

import com.mineinabyss.features.relics.depthmeter.DepthHudSystem
import com.mineinabyss.features.relics.depthmeter.ShowDepthSystem
import com.mineinabyss.features.relics.depthmeter.ToggleDepthHudSystem
import com.mineinabyss.features.relics.sickle.HarvestListener
import com.mineinabyss.features.relics.sickle.SickleListener
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("relics")
class RelicsFeature : AbyssFeature {
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
            ToggleStarCompassHudSystem(),
            ToggleStarCompassHud(),
            HarvestListener()
        )
        listeners(SickleListener())
    }
}
