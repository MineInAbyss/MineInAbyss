package com.mineinabyss.features.relics.grapplinghook

import com.mineinabyss.components.relics.grappling.*
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.bridge.components.RightClicked
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.SourceScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import org.bukkit.entity.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class GearyHookListener : GearyListener() {

    val SourceScope.player by get<Player>()
    private val TargetScope.grapplingHook by get<GrapplingHook>()
    val EventScope.grapplingHook by family { has<RightClicked>() }

    @Handler
    fun SourceScope.doGrapple(target: TargetScope) {
        player.swingMainHand()
        if (player.uniqueId in hookMap) {
            val playerHook = hookMap[player.uniqueId]!!
            playerHook.removeGrapple()
            playerHook.job?.cancel()
            if (playerHook.hookData.type == GrapplingHookType.MANUAL)
                ManualGrapple.stopManualGrapple(player)
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
        val playerHook = PlayerGrapple(hook, target.grapplingHook, player, bat)
        playerHook.sendGrappleLeash()
        hookMap[player.uniqueId] = playerHook


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
}


