package com.mineinabyss.helpers

import com.mineinabyss.components.playerData
import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.mineinabyss.core.MIAConfig
import com.mineinabyss.mineinabyss.core.mineInAbyss
import com.okkero.skedule.schedule
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import kotlin.math.atan2


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
    }
    else Component.text("${Space.of(160)}${splitBalance}:orthcoin:")

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

fun Player.bossbarCompass(loc: Location, bar: BossBar) {
    val player = player ?: return

    val dir = loc.clone().subtract(player.location).toVector()
    val angleDir = (atan2(dir.z,dir.x) / 2 / Math.PI * 360 + 180) % 360
    val angleLook = (atan2(player.location.direction.z,player.location.direction.x) / 2 / Math.PI * 360 + 180) % 360

    when ((angleDir - angleLook + 180) % 360) {
        in 0.0..22.5 -> bar.name(Component.text(":arrow_s:"))
        in 22.5..45.0 -> bar.name(Component.text(":arrow_sse:"))
        in 45.0..67.5 -> bar.name(Component.text(":arrow_sse:"))
        in 67.5..90.0 -> bar.name(Component.text(":arrow_ese:"))
        in 90.0..112.5 -> bar.name(Component.text(":arrow_e:"))
        in 112.5..135.0 -> bar.name(Component.text(":arrow_ene:"))
        in 135.0..147.5 -> bar.name(Component.text(":arrow_ne:"))
        in 157.5..180.0 -> bar.name(Component.text(":arrow_nne:"))
        in 180.0..202.5 -> bar.name(Component.text(":arrow_n:"))
        in 202.5..225.0 -> bar.name(Component.text(":arrow_nnw:"))
        in 225.0..247.5 -> bar.name(Component.text(":arrow_nw:"))
        in 247.5..270.0 -> bar.name(Component.text(":arrow_wnw:"))
        in 270.0..292.5 -> bar.name(Component.text(":arrow_w:"))
        in 292.5..315.0 -> bar.name(Component.text(":arrow_wsw:"))
        in 315.0..337.5 -> bar.name(Component.text(":arrow_sw:"))
        in 337.5..360.0 -> bar.name(Component.text(":arrow_ssw:"))
        else -> bar.name(Component.text(":arrow_null:")) // Meant for quests etc not in the same section
    }

    player.hideBossBar(bar)
    player.showBossBar(bar)
}
