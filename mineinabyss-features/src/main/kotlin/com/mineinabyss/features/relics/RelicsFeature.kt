package com.mineinabyss.features.relics

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

class RelicsFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        geary.pipeline.addSystems(
            ToggleStarCompassHudSystem(),
            ToggleStarCompassHud(),
        )
    }
}
