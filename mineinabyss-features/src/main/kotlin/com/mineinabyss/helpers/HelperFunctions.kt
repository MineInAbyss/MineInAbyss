package com.mineinabyss.helpers

import com.mineinabyss.components.playerData
import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.mineinabyss.core.MIAConfig
import com.mineinabyss.mineinabyss.core.isAbyssWorld
import com.mineinabyss.mineinabyss.core.layer
import com.mineinabyss.mineinabyss.core.mineInAbyss
import com.okkero.skedule.schedule
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*


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

private val recentlyMovedPlayers: MutableSet<UUID> = HashSet()
fun handleCurse(player: Player, from: Location, to: Location) {
    //Arbitrary range with the purpose of preventing curse on section change
    if (from.distanceSquared(to) > 32 * 32) return

    if (recentlyMovedPlayers.contains(player.uniqueId)) {
        recentlyMovedPlayers.remove(player.uniqueId)
        return
    }

    if (!player.world.isAbyssWorld) return

    val changeY = to.y - from.y
    val playerData = player.playerData

    playerData.apply {
        if (player.isInvulnerable) {
            curseAccrued = 0.0
        } else if (playerData.isAffectedByCurse) {
            val layer = to.layer ?: return

            val dist = curseAccrued
            curseAccrued = (dist + changeY).coerceAtLeast(0.0)
            if (dist >= 10) {
                layer.ascensionEffects.forEach {
                    it.clone().applyEffect(player, 10)
                }
                curseAccrued -= 10
            }
        }
    }
}


