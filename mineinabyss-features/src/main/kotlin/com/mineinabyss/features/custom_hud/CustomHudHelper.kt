package com.mineinabyss.features.custom_hud

import com.mineinabyss.components.custom_hud.customHudData
import com.mineinabyss.packy.components.packyData
import io.lumine.mythichud.api.HudHolder
import io.lumine.mythichud.api.MythicHUD
import io.lumine.mythichud.api.element.layout.HudLayout
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.jvm.optionals.getOrNull

internal val mythicHud by lazy { Bukkit.getPluginManager().getPlugin("MythicHUD") as MythicHUD }
val HudHolder.activeLayouts: List<HudLayout> get() = mythicHud.layouts().layouts.mapNotNull { getActiveLayout(it.key).getOrNull()?.parent }
fun CustomHudFeature.customHudEnabled(player: Player) = this.customHudTemplate in player.packyData.enabledPackIds

val Player.hudHolder: HudHolder? get() = HudHolder.get(this)
fun toggleBackgroundLayouts(player: Player, feature: CustomHudFeature) {
    val backgroundLayout = mythicHud.layouts().get(feature.backgroundLayout) ?: return
    val layouts = mythicHud.layouts().defaults.minus(backgroundLayout)

    player.hudHolder?.let { hudHolder ->
        // Clear layouts and add backgrounds back in if they were enabled
        layouts.forEach(hudHolder::removeLayout)
        if (player.customHudData.showBackgrounds) hudHolder.addLayout(backgroundLayout)
        else hudHolder.removeLayout(backgroundLayout)
        layouts.forEach(hudHolder::addLayout)
        hudHolder.reloadLayouts()
        hudHolder.send()
    }
}
