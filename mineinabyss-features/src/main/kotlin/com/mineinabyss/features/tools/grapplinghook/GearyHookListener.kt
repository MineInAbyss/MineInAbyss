package com.mineinabyss.features.tools.grapplinghook

import com.mineinabyss.components.tools.grappling.*
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.bridge.components.RightClicked
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import org.bukkit.entity.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class GearyHookListener : GearyListener() {

    val Pointers.player by get<Player>().on(source)
    private val Pointers.grapplingHook by get<GrapplingHook>().on(target)
    val Pointers.rightClicked by family { has<RightClicked>() }.on(event)

    override fun Pointers.handle() {
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
            hook.velocity = lookDir.multiply(grapplingHook.hookSpeed * 2.0)
            hook.isSilent = true
            hook.shooter = player
        }
        hook.pickupStatus = AbstractArrow.PickupStatus.DISALLOWED
        hook.toGeary().add<GrapplingHookEntity>()

        val bat = summonBat(player)
        bat.toGeary().add<GrapplingHookEntity>()
        player.addPassenger(bat)
        val playerHook = PlayerGrapple(hook, grapplingHook, player, bat)
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


