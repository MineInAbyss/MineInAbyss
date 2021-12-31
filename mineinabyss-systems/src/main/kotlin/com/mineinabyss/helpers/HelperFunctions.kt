package com.mineinabyss.helpers

import com.mineinabyss.components.playerData
import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.mineinabyss.core.MIAConfig
import com.mineinabyss.mineinabyss.core.mineInAbyss
import com.okkero.skedule.schedule
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector


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
        Component.text("\uF83C${splitBalance}:orthcoin: $splitSupporterBalance:clouttoken:")
    }
    else Component.text("\uF83C\uF83A${splitBalance}:orthcoin:")

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


