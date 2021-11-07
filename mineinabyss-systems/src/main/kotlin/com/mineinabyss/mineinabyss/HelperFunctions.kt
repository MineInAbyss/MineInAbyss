package com.mineinabyss.mineinabyss

import com.derongan.minecraft.deeperworld.services.WorldManager
import com.mineinabyss.mineinabyss.core.MIAConfig
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

object NegativeSpace{
    const val PLUS_ONE = "\uF821"
    const val PLUS_TWO = "\uF822"
    const val PLUS_THREE = "\uF823"
    const val PLUS_FOUR = "\uF824"
    const val PLUS_FIVE = "\uF825"
    const val PLUS_SIX = "\uF826"
    const val PLUS_SEVEN = "\uF827"
    const val PLUS_EIGHT = "\uF828"
    const val PLUS_NINE = "\uF829"
    const val PLUS_TEN = "\uF82A"
    const val PLUS_EIGHTEEN = "\uF82B"
    const val PLUS_THIRTYFOUR = "\uF82C"
}