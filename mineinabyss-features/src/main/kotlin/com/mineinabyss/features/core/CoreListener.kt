package com.mineinabyss.features.core

import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.location.up
import io.papermc.paper.event.player.PlayerFailMoveEvent
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.data.Levelled
import org.bukkit.block.data.type.BubbleColumn
import org.bukkit.block.data.type.Light
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import kotlin.random.Random

class CoreListener : Listener {

    @EventHandler
    fun PlayerFailMoveEvent.onMoveWrongly() {
        when (failReason) {
            PlayerFailMoveEvent.FailReason.MOVED_TOO_QUICKLY -> logWarning = false
            PlayerFailMoveEvent.FailReason.MOVED_WRONGLY -> logWarning = false
            else -> {}
        }
    }

    @EventHandler
    fun PlayerMoveEvent.playerMove() {
        if (player.gameMode.isInvulnerable || !hasExplicitlyChangedBlock()) return

        player.location.findLocationAround(radius = 1, scale = 0.30) {
                it.block.isFlowing && it.up(4.0).block.isFlowing
        }?.let {
            //bypass armor damage reduction
            player.damage(0.0001) // trigger damage sound effect
            player.health = (player.health - (0.25 * abyss.config.core.waterfallDamageMultiplier)).coerceAtLeast(0.0)

            player.world.spawnParticle(Particle.CLOUD, player.location.add(0.0, 0.75, 0.0), 1, 0.5, 0.5, 0.5, 0.3)
            player.velocity = player.velocity.apply {
                x = Random.nextDouble(
                    -abyss.config.core.waterfallMoveMultiplier,
                    abyss.config.core.waterfallMoveMultiplier
                )
                y = -0.1
                z = Random.nextDouble(
                    -abyss.config.core.waterfallMoveMultiplier,
                    abyss.config.core.waterfallMoveMultiplier
                )
            }
        }

        player.location.findLocationAround(radius = 1, scale = 0.30) {
            it.clone().add(0.0, 4.0, 0.0).block.isBubbleColumn
        }?.let {
            if (player.maximumAir <= 0) {
                player.remainingAir = player.remainingAir
                player.damage(0.0001) // trigger damage sound effect
                player.health =
                    (player.health - (0.25 * abyss.config.core.bubbleColumnDamageMultiplier)).coerceAtLeast(0.0)

            } else {
                player.remainingAir = (player.maximumAir - abyss.config.core.bubbleColumnBreathMultiplier)
                player.maximumAir = player.remainingAir.coerceAtLeast(0)
            }

        }
        if (!player.isInWaterOrBubbleColumn) player.maximumAir = 300
    }

    private val Block.isFlowing: Boolean get() = ((blockData as? Levelled)?.level ?: 0) >= 8 && blockData !is Light
    private val Block.isBubbleColumn: Boolean get() = blockData is BubbleColumn

    private fun Location.findLocationAround(radius: Int, scale: Double, predicate: (Location) -> Boolean): Location? {
        for (x in -radius..radius) {
            for (z in -radius..radius) {
                val checkLoc = clone().add(x * scale, 0.0, z * scale)
                if (predicate(checkLoc))
                    return checkLoc
            }
        }
        return null
    }
}
