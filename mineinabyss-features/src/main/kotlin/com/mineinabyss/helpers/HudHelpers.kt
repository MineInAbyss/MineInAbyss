package com.mineinabyss.helpers

import com.ehhthan.happyhud.HappyHUD
import com.ehhthan.happyhud.api.HudHolder
import com.mineinabyss.idofront.plugin.isPluginEnabled
import org.bukkit.entity.Player

val happyHUD: HappyHUD = HappyHUD.getInstance()
val Player.hudHolder: HudHolder get() = HudHolder.get(this)
val Player.isHudHolder: Boolean get() = HudHolder.has(this)

fun Player.toggleHud(layoutId: String, toggle: Boolean) {
    if (!isPluginEnabled("HappyHUD") || !this.isHudHolder) return
    val layout = happyHUD.layouts().get(layoutId) ?: return

    if (toggle) hudHolder.addLayout(layout)
    else hudHolder.removeLayout(layout)
    hudHolder.updateAll()
    hudHolder.send()
}
