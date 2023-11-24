package com.mineinabyss.features.custom_hud

import com.ehhthan.happyhud.HappyHUD
import com.ehhthan.happyhud.api.HudHolder
import com.mineinabyss.components.custom_hud.customHudData
import com.mineinabyss.packy.components.packyData
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class CustomHudHelper

private val happyHud by lazy { Bukkit.getPluginManager().getPlugin("HappyHUD") as HappyHUD }
private val LAYOUT_KEY = NamespacedKey.fromString("hud_layouts", happyHud)!!
private val Player.activeLayouts get() = persistentDataContainer.get(LAYOUT_KEY, PersistentDataType.TAG_CONTAINER)?.keys?.mapNotNull { happyHud.layouts().get(it.key) } ?: emptyList()
fun CustomHudFeature.customHudEnabled(player: Player) = this.customHudTemplate in player.packyData.enabledPackAddons.map { it.id }

val Player.hudHolder: HudHolder? get() = HudHolder.holders().find { it.player().uniqueId == uniqueId }
fun toggleBackgroundLayouts(player: Player, feature: CustomHudFeature) {
    val backgroundLayout = happyHud.layouts().layouts.find { it.key == feature.backgroundLayout } ?: return
    val oldActiveLayouts = player.activeLayouts.toMutableList().apply { remove(backgroundLayout) }

    player.hudHolder?.let { hudHolder ->
        // Clear layouts and add backgrounds back in if they were enabled
        oldActiveLayouts.forEach(hudHolder::removeLayout)
        when {
            player.customHudData.showBackgrounds -> hudHolder.addLayout(backgroundLayout)
            else -> hudHolder.removeLayout(backgroundLayout)
        }
        oldActiveLayouts.forEach(hudHolder::addLayout)
        hudHolder.send()
    }
}
