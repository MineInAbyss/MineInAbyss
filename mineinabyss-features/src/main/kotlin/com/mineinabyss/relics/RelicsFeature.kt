package com.mineinabyss.relics

import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.geary
import com.mineinabyss.relics.depthmeter.DepthHudSystem
import com.mineinabyss.relics.depthmeter.RemoveDepthMeterHud
import com.mineinabyss.relics.depthmeter.ShowDepthSystem
import com.mineinabyss.relics.depthmeter.ToggleDepthHudSystem
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
        registerEvents(SickleListener())
    }
}
