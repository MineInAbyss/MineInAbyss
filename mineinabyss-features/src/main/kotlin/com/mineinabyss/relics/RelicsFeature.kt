package com.mineinabyss.relics

import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.*
import com.mineinabyss.relics.depthmeter.*
import com.mineinabyss.relics.sickle.HarvestListener
import com.mineinabyss.relics.sickle.SickleListener
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
        geary {
            systems(
                ShowDepthSystem(),
                ToggleDepthHudSystem(),
                DepthHudSystem(this@RelicsFeature),
                RemoveDepthMeterHud(this@RelicsFeature),
                ToggleStarCompassHudSystem(this@RelicsFeature),
                RemoveStarCompassBar(this@RelicsFeature),
                HarvestListener()
            )
        }
        listeners(SickleListener())
    }
}
