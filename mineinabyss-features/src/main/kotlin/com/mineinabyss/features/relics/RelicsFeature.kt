package com.mineinabyss.features.relics

import com.mineinabyss.features.relics.depthmeter.DepthHudSystem
import com.mineinabyss.features.relics.depthmeter.RemoveDepthMeterHud
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
class RelicsFeature(
    val depthHudId: String = "depth",
    val layerHudId: String = "layer",
    val starcompassHudId: String = "starcompass"
) : AbyssFeature {
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
            DepthHudSystem(this@RelicsFeature),
            RemoveDepthMeterHud(this@RelicsFeature),
            ToggleStarCompassHudSystem(this@RelicsFeature),
            RemoveStarCompassBar(this@RelicsFeature),
            HarvestListener()
        )
        listeners(SickleListener())
    }
}
