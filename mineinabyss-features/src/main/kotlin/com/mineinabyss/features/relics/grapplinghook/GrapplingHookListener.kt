package com.mineinabyss.features.relics.grapplinghook

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.mineinabyss.components.relics.grappling.*
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.bridge.components.RightClicked
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.SourceScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.mineinabyss.core.abyss
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.Sound
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.event.player.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import kotlin.math.round
import kotlin.math.roundToInt

class GrapplingHookListener : GearyListener(), Listener {
    val SourceScope.player by get<Player>()
    private val TargetScope.grapplingHook by get<GrapplingHook>()
    val EventScope.grapplingHook by family { has<RightClicked>() }

    @Handler
    fun SourceScope.doGrapple(target: TargetScope) {
        player.swingMainHand()
        if (player.uniqueId in hookMap) {
            hookMap[player.uniqueId]?.removeGrapple()
            return
        }

        val lookDir = player.eyeLocation.direction
        val hook = player.world.spawn(player.eyeLocation.add(lookDir), Arrow::class.java) { hook ->
            hook.isPersistent = false
            hook.velocity = lookDir.multiply(target.grapplingHook.hookSpeed * 2.0)
            hook.isSilent = true
            hook.shooter = player
        }
        hook.pickupStatus = AbstractArrow.PickupStatus.DISALLOWED
        hook.toGeary().add<GrapplingHookEntity>()

        val bat = summonBat(player)
        bat.toGeary().add<GrapplingHookEntity>()
        bat.setLeashHolder(hook)
        hookMap[player.uniqueId] = PlayerGrapple(hook, target.grapplingHook, player, bat)
    }

    private fun summonBat(link: Entity): Bat {
        return link.world.spawn(link.location, Bat::class.java) { bat ->
            bat.isSilent = true
            bat.setAI(false)
            bat.isInvulnerable = true
            bat.isCollidable = false
            bat.isAwake = false
            bat.isAware = false
            bat.isPersistent = false
            bat.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Int.MAX_VALUE, Int.MAX_VALUE, false, false))
        }
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() = hookMap[player.uniqueId]?.removeGrapple()

    @EventHandler
    fun PlayerQuitEvent.onQuit() = hookMap[player.uniqueId]?.removeGrapple()

    @EventHandler
    fun PlayerDeathEvent.onDeath() = hookMap[player.uniqueId]?.removeGrapple()

    @EventHandler
    fun PlayerMoveEvent.onMoveWithGrappleShot() = hookMap[player.uniqueId]?.moveBatToPlayer()

    @EventHandler
    fun PlayerSwapHandItemsEvent.onSwapWithGrappleShot() = hookMap[player.uniqueId]?.removeGrapple()

    @EventHandler
    fun PlayerItemHeldEvent.onSwapWithGrappleShot() = hookMap[player.uniqueId]?.removeGrapple()

    @EventHandler
    fun EntityDropItemEvent.onBatDropLeash() =
        (entity as? Bat)?.toGearyOrNull()?.get<GrapplingHookEntity>()?.let { isCancelled = true }

    @EventHandler
    fun BatToggleSleepEvent.onBatAwake() = entity.toGeary().get<GrapplingHookEntity>()?.let { isCancelled = true }

    @EventHandler
    fun EntityDamageByEntityEvent.onPlayerHitBat() {
        val bat = entity as? Bat ?: return
        if (bat.toGearyOrNull()?.has<GrapplingHookEntity>() == true) isCancelled = true
        val player = damager as? Player ?: return

        isCancelled = true
        hookMap[player.uniqueId]?.removeGrapple()
    }

    @EventHandler
    fun ProjectileHitEvent.onHit() {
        val arrow = entity as? Arrow ?: return
        val player = arrow.shooter as? Player ?: return
        if (arrow.toGearyOrNull()?.has<GrapplingHookEntity>() != true) return
        if (hitBlock == null) return
        val playerHook = hookMap[player.uniqueId] ?: return

        if (player.location.distance(playerHook.hook.location) > playerHook.hookData.range) {
            playerHook.removeGrapple()
            return
        }

        val maxCount = round(playerHook.hookData.pullStrength * 20).roundToInt()
        val (anchor, particle) = playerHook.hook to playerHook.player
        val (pBatAdd, aBatAdd) = particle.height * 0.5 to anchor.height * 0.5
        hookMap[player.uniqueId] = playerHook.copy(job = when(playerHook.hookData.type) {
            GrapplingHookType.MECHANICAL -> mechanicalHookJob(maxCount, player, playerHook, anchor, particle, pBatAdd, aBatAdd)
            GrapplingHookType.MANUAL -> manualHookJob(maxCount, player, playerHook, anchor, particle, pBatAdd, aBatAdd)
        })
    }

    private fun manualHookJob(maxCount: Int, player: Player, playerHook: PlayerGrapple, anchor: Arrow, particle: Player, pBatAdd: Double, aBatAdd: Double): Job {
        return abyss.plugin.launch {

        }
    }

    private fun mechanicalHookJob(maxCount: Int, player: Player, playerHook: PlayerGrapple, anchor: Arrow, particle: Player, pBatAdd: Double, aBatAdd: Double): Job {
        return abyss.plugin.launch {
            var counter = 0
            do {
                counter++
                if (counter > maxCount && !playerHook.bat.isDead && !anchor.isDead && !particle.isDead) {
                    playerHook.removeGrapple()
                    return@launch
                }

                val (pLoc, aLoc) = player.location.clone().apply { y+= pBatAdd } to playerHook.hook.location.clone().apply { y+= aBatAdd }
                val (pVector, aVector) = pLoc.toVector() to aLoc.toVector()
                var (prox, superProx) = playerHook.isProx(pLoc, aLoc, player.isSneaking) to playerHook.isProx(pLoc, aLoc, true, 0.1)
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
                        playerHook.player.location.clone().apply { y+= playerHook.batAddY },
                        Sound.ITEM_CROSSBOW_LOADING_MIDDLE,
                        0.5f,
                        1.1f
                    )
                }
                particle.velocity = vector
                delay(1.ticks)
            } while (counter <= maxCount && !playerHook.bat.isDead && !playerHook.hook.isDead && !player.isDead)
        }
    }
}