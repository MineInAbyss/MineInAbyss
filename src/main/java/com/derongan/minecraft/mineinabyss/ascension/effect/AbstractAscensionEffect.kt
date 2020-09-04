package com.derongan.minecraft.mineinabyss.ascension.effect

import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.okkero.skedule.schedule
import org.bukkit.entity.Player

abstract class AbstractAscensionEffect : AscensionEffect {
    abstract val offset: Long
    abstract val iterations: Int

    var elapsed = 0

    override fun applyEffect(player: Player, ticks: Int) {
            mineInAbyss.schedule {
                waitFor(offset)
                repeat(iterations) {
                    applyEffect(player)
                    waitFor(duration.toLong())
                }
        }
    }

    abstract fun applyEffect(player: Player)

    override val isDone: Boolean
        get() = duration <= 0

    override fun cleanUp(player: Player) {}
}