package com.mineinabyss.enchants

import com.derongan.minecraft.mineinabyss.systems.BaneOfKuongatariListener
import com.derongan.minecraft.mineinabyss.systems.BirdSwatterListener
import com.derongan.minecraft.mineinabyss.systems.JawBreakerListener
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.geary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@ExperimentalCommandDSL
@Serializable
@SerialName("enchants")
class EnchantsFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        CustomEnchants.register()

        registerEvents(
            SoulBoundListener(),
            FrostAspectListener(),
            BaneOfKuongatariListener(),
            BirdSwatterListener(),
            JawBreakerListener(),
        )

        geary {
            systems(SoulSystem())
            CustomEnchantCommandExecutor()
        }
    }
}
