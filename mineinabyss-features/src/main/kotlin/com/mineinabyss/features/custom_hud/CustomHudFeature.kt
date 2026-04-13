package com.mineinabyss.features.custom_hud

import com.mineinabyss.dependencies.*
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.requirePlugins
import com.mineinabyss.packy.components.packyData
import org.bukkit.entity.Player

val CustomHudFeature = module("custom-hud") {
    val backgroundLayout: String = "backgrounds" //TODO move to config
    val customHudTemplate: String = "custom_hud"
    requirePlugins("MythicHUD", "Packy")
    require(get<AbyssFeatureConfig>().customHud.enabled) { "Custom HUD feature is disabled" }

    single<CustomHudModule> {
        object : CustomHudModule {
            override fun customHudEnabled(player: Player): Boolean = customHudTemplate in player.packyData.enabledPackIds
        }
    }

    listeners(new(::CustomHudListener))
}.gets<CustomHudModule>()

interface CustomHudModule {
    fun customHudEnabled(player: Player): Boolean
}