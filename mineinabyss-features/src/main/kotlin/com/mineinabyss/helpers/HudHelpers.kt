package com.mineinabyss.helpers

import com.ehhthan.happyhud.HappyHUD
import com.ehhthan.happyhud.api.HudHolder
import com.mineinabyss.deeperworld.world.section.Section
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.idofront.plugin.isPluginEnabled
import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.math.atan2

val happyHUD: HappyHUD = HappyHUD.getInstance()
val Player.hudHolder: HudHolder get() = HudHolder.get(this)

fun Player.toggleHud(layoutId: String, toggle: Boolean) {
    if (!isPluginEnabled("HappyHUD") || !hudHolder.player().isOnline) return
    val layout = happyHUD.layouts().get(layoutId) ?: return

    if (toggle) hudHolder.addLayout(layout)
    else hudHolder.removeLayout(layout)
}

fun Player.getCompassAngleUnicode() : String {
    val loc = location.section?.getSectionCenter() ?: return ""
    if (world != loc.world) return ""

    val dir = loc.subtract(location).toVector()
    val angleDir = (atan2(dir.z, dir.x) / 2 / Math.PI * 360 + 180) % 360
    val angleLook = (atan2(location.direction.z, location.direction.x) / 2 / Math.PI * 360 + 180) % 360

    return barUnicodeList[(((angleDir - angleLook + 360) % 360) / 22.5).toInt()]
}

fun Section.getSectionCenter() : Location {
    val center = region.center
    return Location(world, center.x.toDouble(), 0.0, center.z.toDouble())
}

private val barUnicodeList = listOf(
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
)
