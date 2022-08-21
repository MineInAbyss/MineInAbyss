package com.mineinabyss.helpers

import com.ehhthan.happyhud.api.HudHolder
import com.mineinabyss.components.playerData
import com.mineinabyss.idofront.messaging.miniMsg
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.math.atan2

fun Player.toggleHud(toggle: Boolean? = null) {
    if (toggle == null) playerData.showPlayerBalance = !playerData.showPlayerBalance
    else playerData.showPlayerBalance = toggle

    //TODO Fix this whenever a method is exposed to add a new HudHolder
    /*if (playerData.showPlayerBalance && !HudHolder.has(this))
        HudHolder.holders().add(this as HudHolder)
    else */if (!playerData.showPlayerBalance && HudHolder.has(this))
        HudHolder.holders().remove(HudHolder.get(this))
}

fun Player.bossbarCompass(loc: Location?, bar: BossBar) {
    bar.name(getCompassAngle(loc))
    showBossBar(bar)
}

private fun Player.getCompassAngle(loc: Location?) : Component {
    if (loc == null || world != loc.world)
        return Component.text(":arrow_null:")

    val dir = loc.subtract(location).toVector()
    val angleDir = (atan2(dir.z, dir.x) / 2 / Math.PI * 360 + 180) % 360
    val angleLook = (atan2(location.direction.z, location.direction.x) / 2 / Math.PI * 360 + 180) % 360

    return barNameList[(((angleDir - angleLook + 360) % 360) / 22.5).toInt()].miniMsg()
}

private val barNameList = listOf(
    ":arrow_n:",
    ":arrow_nne:",
    ":arrow_ne:",
    ":arrow_ene:",
    ":arrow_e:",
    ":arrow_ese:",
    ":arrow_se:",
    ":arrow_sse:",
    ":arrow_s:",
    ":arrow_ssw:",
    ":arrow_sw:",
    ":arrow_wsw:",
    ":arrow_w:",
    ":arrow_wnw:",
    ":arrow_nw:",
    ":arrow_nnw:",
)
