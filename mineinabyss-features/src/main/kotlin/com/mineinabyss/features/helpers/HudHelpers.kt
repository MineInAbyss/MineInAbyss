package com.mineinabyss.features.helpers

import com.ehhthan.happyhud.HappyHUD
import com.ehhthan.happyhud.api.HudHolder
import com.mineinabyss.mineinabyss.core.abyss
import org.bukkit.Bukkit
import org.bukkit.entity.Player

val happyHUD: HappyHUD by lazy { Bukkit.getPluginManager().getPlugin("HappyHUD") as HappyHUD }
val Player.hudHolder: HudHolder get() = HudHolder.get(this)
val Player.isHudHolder: Boolean get() = HudHolder.has(this)

fun Player.changeHudStates(layoutIds: List<String>, state: Boolean) = layoutIds.forEach { changeHudState(it, state) }

fun Player.changeHudState(layoutId: String, state: Boolean) {
    if (!abyss.isHappyHUDEnabled || !this.isHudHolder) return
    val layout = happyHUD.layouts().get(layoutId) ?: return

    if (state) hudHolder.addLayout(layout)
    else hudHolder.removeLayout(layout)
    hudHolder.reloadLayouts()
    hudHolder.send()
}
