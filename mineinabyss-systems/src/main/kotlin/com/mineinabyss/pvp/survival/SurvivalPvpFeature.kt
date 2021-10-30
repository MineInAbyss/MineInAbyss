package com.mineinabyss.pvp.survival

import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.geary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("survival_pvp")
@ExperimentalCommandDSL
class SurvivalPvpFeature: AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(SurvivalPvpListener())

        geary {
            SurvivalPvpCommandExecutor()
        }
    }
}