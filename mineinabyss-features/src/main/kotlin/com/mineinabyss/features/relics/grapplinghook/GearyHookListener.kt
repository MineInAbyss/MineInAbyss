package com.mineinabyss.features.relics.grapplinghook

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.mineinabyss.components.relics.grappling.GrapplingHook
import com.mineinabyss.components.relics.grappling.GrapplingHookEntity
import com.mineinabyss.components.relics.grappling.PlayerGrapple
import com.mineinabyss.components.relics.grappling.hookMap
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.bridge.components.RightClicked
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.SourceScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.protocolburrito.dsl.sendTo
import org.bukkit.Bukkit
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

        hookMap[player.uniqueId] = PlayerGrapple(hook, target.grapplingHook, player, bat)

        val leashEntity = PacketContainer(PacketType.Play.Server.ATTACH_ENTITY)
        leashEntity.integers.write(0, bat.entityId)
        leashEntity.integers.write(1, hook.entityId)
        Bukkit.getOnlinePlayers().forEach { leashEntity.sendTo(it) }
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
