package com.mineinabyss.pvp.survival

import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.geary
import com.mineinabyss.pvp.adventure.AdventurePvpCommandExecutor
import com.mineinabyss.pvp.adventure.AdventurePvpListener
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("adventure_pvp")
@ExperimentalCommandDSL
class AdventurePvpFeature: AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(AdventurePvpListener())

        geary {
            AdventurePvpCommandExecutor()
        }
    }
}