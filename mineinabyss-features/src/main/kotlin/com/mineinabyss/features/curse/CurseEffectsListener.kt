package com.mineinabyss.features.curse

import com.mineinabyss.features.curse.effects.MaxHealthChangeEffect
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class CurseEffectsListener : Listener {
    @EventHandler
    fun PlayerQuitEvent.fixMaxHealthEffectOnLeave() {
        player.getAttribute(Attribute.MAX_HEALTH)?.run {
            modifiers.filter {
                it.key == MaxHealthChangeEffect.CURSE_MAX_HEALTH && it.key !in MaxHealthChangeEffect.activeEffects
            }.forEach(::removeModifier)
        }
    }
}
