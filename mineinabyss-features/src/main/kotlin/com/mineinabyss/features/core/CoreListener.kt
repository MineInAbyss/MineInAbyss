package com.mineinabyss.features.core

import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.idofront.location.up
import io.papermc.paper.event.player.PlayerFailMoveEvent
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.data.Levelled
import org.bukkit.block.data.type.BubbleColumn
import org.bukkit.block.data.type.Light
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.vehicle.VehicleMoveEvent
import kotlin.random.Random

class CoreListener(
    val config: AbyssFeatureConfig,
) : Listener {
    @EventHandler
    fun PlayerFailMoveEvent.onMoveWrongly() {
        when (failReason) {
            PlayerFailMoveEvent.FailReason.MOVED_TOO_QUICKLY -> logWarning = false
            PlayerFailMoveEvent.FailReason.MOVED_WRONGLY -> logWarning = false
            else -> {}
        }
    }

    @EventHandler
    fun VehicleMoveEvent.onMove() {
        vehicle.passengers.filterIsInstance<Player>().forEach { player ->
            if (player.gameMode.isInvulnerable) return@forEach

            player.location.findLocationAround(1, 0.30) {
                it.block.isFlowing && it.up(4.0).block.isFlowing
            }?.also { player.handleWaterfall() }

            player.location.findLocationAround(1, 0.30) {
                it.clone().add(0.0, 4.0, 0.0).block.isBubbleColumn
            }?.also { player.handleBubbleColumn() }

            if (!player.isInWater) player.maximumAir = 300
        }
    }

    @EventHandler
    fun PlayerMoveEvent.playerMove() {
        if (player.gameMode.isInvulnerable || !hasExplicitlyChangedBlock()) return

        player.location.findLocationAround(1, 0.30) {
            it.block.isFlowing && it.up(4.0).block.isFlowing
        }?.also { player.handleWaterfall() }

        player.location.findLocationAround(radius = 1, scale = 0.30) {
            it.clone().add(0.0, 4.0, 0.0).block.isBubbleColumn
        }?.also { player.handleBubbleColumn() }

        if (!player.isInWater) player.maximumAir = 300
    }

    private fun Player.handleWaterfall() {
        //bypass armor damage reduction
        damage(0.0001) // trigger damage sound effect
        health = (health - (0.25 * config.core.waterfallDamageMultiplier)).coerceAtLeast(0.0)

        world.spawnParticle(Particle.CLOUD, location.add(0.0, 0.75, 0.0), 1, 0.5, 0.5, 0.5, 0.3)
        velocity = velocity.apply {
            x = Random.nextDouble(
                -config.core.waterfallMoveMultiplier,
                config.core.waterfallMoveMultiplier
            )
            y = -0.1
            z = Random.nextDouble(
                -config.core.waterfallMoveMultiplier,
                config.core.waterfallMoveMultiplier
            )
        }
    }

    private fun Player.handleBubbleColumn() {
        if (maximumAir <= 0) {
            remainingAir = remainingAir
            damage(0.0001) // trigger damage sound effect
            health =
                (health - (0.25 * config.core.bubbleColumnDamageMultiplier)).coerceAtLeast(0.0)

        } else {
            remainingAir = (maximumAir - config.core.bubbleColumnBreathMultiplier)
            maximumAir = remainingAir.coerceAtLeast(0)
        }

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
