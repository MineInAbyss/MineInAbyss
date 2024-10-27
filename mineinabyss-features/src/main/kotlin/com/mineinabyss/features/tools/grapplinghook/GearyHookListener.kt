package com.mineinabyss.features.tools.grapplinghook

import com.mineinabyss.components.tools.grappling.*
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.modules.observeWithData
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.systems.query.query
import org.bukkit.entity.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


fun Geary.createHookAction() = observeWithData<GrapplingHook>().exec(query<Player>()) { (player) ->
    player.swingMainHand()
    if (player.uniqueId in hookMap) {
        val playerHook = hookMap[player.uniqueId]!!
        playerHook.removeGrapple()
        playerHook.job?.cancel()
        if (playerHook.hookData.type == GrapplingHookType.MANUAL)
            ManualGrapple.stopManualGrapple(player)
        return@exec
    }

    val lookDir = player.eyeLocation.direction
    val hook = player.world.spawn(player.eyeLocation.add(lookDir), Arrow::class.java) { hook ->
        hook.isPersistent = false
        hook.velocity = lookDir.multiply(event.hookSpeed * 2.0)
        hook.isSilent = true
        hook.shooter = player
    }
    hook.pickupStatus = AbstractArrow.PickupStatus.DISALLOWED
    hook.toGeary().add<GrapplingHookEntity>()

    val bat = summonBat(player)
    bat.toGeary().add<GrapplingHookEntity>()
    player.addPassenger(bat)
    val playerHook = PlayerGrapple(hook, event, player, bat)
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
