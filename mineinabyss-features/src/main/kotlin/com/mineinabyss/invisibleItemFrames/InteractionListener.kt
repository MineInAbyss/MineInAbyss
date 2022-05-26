package com.mineinabyss.invisibleItemFrames

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent
import com.mineinabyss.idofront.messaging.broadcast
import net.bytebuddy.implementation.bytecode.Throw
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.ThrownPotion
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.jetbrains.exposed.sql.intLiteral
import org.jetbrains.exposed.sql.intParam

class InteractionListener : Listener {
    @EventHandler
    fun ProjectileHitEvent.onDouseItemFrame() {
        val entity = entity as? ThrownPotion ?: return

        if (hitEntity !is ItemFrame) return
        if (
                entity
                    .potionMeta
                    .basePotionData
                    .type
                    .effectType != PotionEffectType.INVISIBILITY
        ) return

        hitEntity
            ?.location
            ?.getNearbyEntitiesByType(
                ItemFrame::class.java, 1.0
            )?.forEach { frame ->
                frame.isVisible = false
            }
    }
}