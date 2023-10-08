package com.mineinabyss.features.tools.grapplinghook

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.mineinabyss.components.tools.grappling.GrapplingHookEntity
import com.mineinabyss.components.tools.grappling.GrapplingHookType
import com.mineinabyss.components.tools.grappling.PlayerGrapple
import com.mineinabyss.components.tools.grappling.hookMap
import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.deeperworld.world.section.inSectionTransition
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.mineinabyss.core.abyss
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Arrow
import org.bukkit.entity.Bat
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.BatToggleSleepEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.util.Vector
import kotlin.math.round
import kotlin.math.roundToInt

class GrapplingHookListener : Listener {

    @EventHandler
    fun ProjectileHitEvent.onHit() {
        val arrow = entity as? Arrow ?: return
        val player = arrow.shooter as? Player ?: return
        if (arrow.toGearyOrNull()?.has<GrapplingHookEntity>() != true) return
        if (hitBlock == null) return
        val playerHook = hookMap[player.uniqueId] ?: return

        if (playerHook.hook.location.inSectionTransition) {
            playerHook.removeGrapple()
            return
        }

        if (player.location.distance(playerHook.hook.location) > playerHook.hookData.range) {
            playerHook.removeGrapple()
            return
        }
    }

    @EventHandler
    fun PlayerJumpEvent.onJump() {
        val playerHook = hookMap[player.uniqueId] ?: return
        val maxCount = round(playerHook.hookData.pullStrength * 20).roundToInt()
        val (anchor, particle) = playerHook.hook to playerHook.player
        val (pBatAdd, aBatAdd) = particle.height * 0.5 to anchor.height * 0.5

        playerHook.job?.cancel()

        hookMap[player.uniqueId] = playerHook.copy(
            job = when (playerHook.hookData.type) {
                GrapplingHookType.MECHANICAL -> mechanicalHookJob(
                    maxCount,
                    player,
                    playerHook,
                    anchor,
                    particle,
                    pBatAdd,
                    aBatAdd
                )

                GrapplingHookType.MANUAL -> {
                    player.flySpeed = if (playerHook.isOverHook() && !player.isSneaking) -0.01f else 0.03f
                    if (playerHook.isBeneathHook()) {
                        if (player.velocity.y in -0.08..-0.07)
                            player.velocity = player.velocity.add(Vector(0.0, 0.25, 0.0))
                        player.isGrappling = true
                        player.allowFlight = true
                        player.isFlying = true
                        manualHookJob(player)
                    } else if (!playerHook.isOverHook()) ManualGrapple.isGrappling.remove(player.uniqueId)
                    null
                }
            }
        )
    }

    private fun manualHookJob(player: Player): Job {
        return abyss.plugin.launch {
            val playerHook = hookMap[player.uniqueId] ?: return@launch
            player.isGrappling = true
            do {
                Bukkit.getPlayer(player.uniqueId)?.let {
                    it.flySpeed = if (playerHook.isOverHook() && !it.isSneaking) -0.01f else 0.03f
                    if (playerHook.isBeneathHook() && it.isGrappling) {
                        it.allowFlight = true
                        it.isFlying = true
                        it.isGrappling = true
                    } else if (!playerHook.isOverHook()) {
                        it.isGrappling = false
                        it.isFlying = false
                        it.allowFlight = false
                    }
                }

                delay(1.ticks)
            } while (player.isGrappling && !player.isDead)
            ManualGrapple.stopManualGrapple(player)
        }
    }

    private fun mechanicalHookJob(
        maxCount: Int,
        player: Player,
        playerHook: PlayerGrapple,
        anchor: Arrow,
        particle: Player,
        pBatAdd: Double,
        aBatAdd: Double
    ): Job {
        return abyss.plugin.launch {
            var counter = 0
            do {
                if (!player.isSneaking) counter++
                if (counter > maxCount && !playerHook.bat.isDead && !anchor.isDead && !particle.isDead) {
                    playerHook.removeGrapple()
                    return@launch
                }

                // Check repelled length
                if (anchor.location.y - player.eyeLocation.y >= playerHook.hookData.range) {
                    playerHook.removeGrapple()
                    return@launch
                }

                val (pLoc, aLoc) = player.location.clone().apply { y += pBatAdd } to playerHook.hook.location.clone()
                    .apply { y += aBatAdd }
                val (pVector, aVector) = pLoc.toVector() to aLoc.toVector()
                var (prox, superProx) = playerHook.isProx(pLoc, aLoc, player.isSneaking) to playerHook.isProx(
                    pLoc,
                    aLoc,
                    true,
                    0.1
                )
                val particleAboveAnchor = aLoc.y <= pLoc.y && !player.isInWater
                var multConst = 0.65

                var vector = aVector.subtract(pVector)
                if (particleAboveAnchor) {
                    vector.setY(0.0)
                    prox = playerHook.isProx(pLoc, aLoc, true)
                }
                vector.normalize()

                if (prox) multConst = if (superProx) 0.0 else 0.1
                else {
                    if (anchor.isInWater) multConst = 0.3
                    if (!particleAboveAnchor) vector = particle.velocity.normalize().add(vector).normalize()
                }

                if (particleAboveAnchor) {
                    vector =
                        if (playerHook.player.isSneaking) particle.velocity
                        else {
                            vector.multiply(multConst * playerHook.hookData.pullSpeed)
                            Vector(vector.x, particle.velocity.y, vector.z)
                        }
                } else {
                    if (vector.x.isNaN()) vector.setX(0.0)
                    if (vector.y.isNaN()) vector.setY(0.1)
                    if (vector.z.isNaN()) vector.setZ(0.0)
                    vector.multiply(multConst * playerHook.hookData.pullSpeed)
                    particle.fallDistance = 0f
                }
                if (playerHook.player.isSneaking) {
                    if (vector.y < 0.01) vector.setY(-0.6)
                    else vector.setY(vector.y * -0.6)
                } else if (counter % 8 == 0) {
                    playerHook.player.world.playSound(
                        playerHook.player.location.clone().apply { y += playerHook.batAddY },
                        Sound.ITEM_CROSSBOW_LOADING_MIDDLE,
                        0.5f,
                        1.1f
                    )
                }
                particle.velocity = vector
                playerHook.sendGrappleLeash()
                delay(1.ticks)
            } while (counter <= maxCount && !playerHook.bat.isDead && !playerHook.hook.isDead && !player.isDead)
        }
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        hookMap[player.uniqueId]?.removeGrapple()
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        hookMap[player.uniqueId]?.removeGrapple()
    }

    @EventHandler
    fun PlayerDeathEvent.onDeath() {
        hookMap[player.uniqueId]?.removeGrapple()
    }
    @EventHandler
    fun PlayerSwapHandItemsEvent.onSwapWithGrappleShot() {
        hookMap[player.uniqueId]?.removeGrapple()
    }
    @EventHandler
    fun PlayerItemHeldEvent.onSwapWithGrappleShot() {
        hookMap[player.uniqueId]?.removeGrapple()
    }
    @EventHandler
    fun BatToggleSleepEvent.onBatAwake() {
        entity.toGeary().get<GrapplingHookEntity>()?.let { isCancelled = true }
    }
    @EventHandler
    fun EntityDamageByEntityEvent.onPlayerHitBat() {
        val bat = entity as? Bat ?: return
        if (bat.toGearyOrNull()?.has<GrapplingHookEntity>() == true) isCancelled = true
        val player = damager as? Player ?: return

        isCancelled = true
        hookMap[player.uniqueId]?.removeGrapple()
    }

    @EventHandler
    fun PlayerAscendEvent.onPlayerChangeSection() {
        hookMap[player.uniqueId]?.removeGrapple()
    }
    @EventHandler
    fun PlayerDescendEvent.onPlayerChangeSection() {
        hookMap[player.uniqueId]?.removeGrapple()
    }
}
