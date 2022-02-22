package com.mineinabyss.helpers

import com.mineinabyss.components.playerData
import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.mineinabyss.core.MIAConfig
import com.mineinabyss.mineinabyss.core.mineInAbyss
import com.okkero.skedule.schedule
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.function.Predicate
import kotlin.math.cos
import kotlin.math.sin


fun dropItems(loc: Location, drop: ItemStack) {
    loc.world.dropItem(loc.add(Vector.getRandom().subtract(Vector(.5, .5, .5)).multiply(0.5)), drop).velocity =
        Vector.getRandom().add(Vector(-.5, +.5, -.5)).normalize().multiply(.15)
}

data class ItemDrop(
    val material: Material,
    val dropAmount: IntRange,
    val applyFortune: Boolean = true
)

fun Player.isInHub() = MIAConfig.data.hubSection == player?.location?.let { WorldManager.getSectionFor(it) }

fun Player.updateBalance() {
    val data = player?.playerData
    val orthCoinBalance = data?.orthCoinsHeld
    val cloutBalance = data?.cloutTokensHeld
    val splitBalance = orthCoinBalance.toString().toList().joinToString { ":$it:" }.replace(", ", "")
    val splitSupporterBalance = cloutBalance.toString().toList().joinToString { ":$it:" }.replace(", ", "")

    val currentBalance: Component =
        if (data?.cloutTokensHeld!! > 0) {
            /* Switch to NegativeSpace.PLUS when that is added to Idofront */
            Component.text("${Space.of(128)}${splitBalance}:orthcoin: $splitSupporterBalance:clouttoken:")
        } else Component.text("${Space.of(160)}${splitBalance}:orthcoin:")

    if (data.orthCoinsHeld < 0) data.orthCoinsHeld = 0

    mineInAbyss.schedule {
        do {
            player?.sendActionBar(currentBalance)
            waitFor(20)
        } while ((data.orthCoinsHeld == orthCoinBalance && data.cloutTokensHeld == cloutBalance) && data.showPlayerBalance
        )
        return@schedule
    }
}

fun spawnParticleAlongLine(
    start: Location,
    end: Location,
    particle: Particle?,
    pointsPerLine: Int,
    particleCount: Int,
    offsetX: Double,
    offsetY: Double,
    offsetZ: Double,
    extra: Double,
    data: Double?,
    forceDisplay: Boolean,
    operationPerPoint: Predicate<Location?>?
) {
    val d = start.distance(end) / pointsPerLine
    for (i in 0..pointsPerLine) {
        val loc = start.clone()
        val direction = end.toVector().subtract(start.toVector()).normalize()
        val v = direction.multiply(i * d)
        loc.add(v.x, v.y, v.z)
        if (operationPerPoint == null) {
            start.world.spawnParticle(
                particle!!,
                loc,
                particleCount,
                offsetX,
                offsetY,
                offsetZ,
                extra,
                data,
                forceDisplay
            )
            continue
        }
        if (operationPerPoint.test(loc)) {
            start.world.spawnParticle(
                particle!!,
                loc,
                particleCount,
                offsetX,
                offsetY,
                offsetZ,
                extra,
                data,
                forceDisplay
            )
        }
    }
}

fun getRightSide(location: Location, distance: Double): Location {
    val angle = location.yaw / 60
    return location.clone()
        .subtract(Vector(cos(angle.toDouble()), 0.0, sin(angle.toDouble())).normalize().multiply(distance))
        .subtract(0.0, 0.4, 0.0)
}


