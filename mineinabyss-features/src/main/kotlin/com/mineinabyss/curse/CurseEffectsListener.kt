package com.mineinabyss.curse

import com.mineinabyss.curse.effects.MaxHealthChangeEffect
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class CurseEffectsListener : Listener {
    @EventHandler
    fun PlayerQuitEvent.fixMaxHealthEffectOnLeave() {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.run {
            modifiers.filter {
                it.name == MaxHealthChangeEffect.CURSE_MAX_HEALTH && it !in MaxHealthChangeEffect.activeEffects
            }.forEach(::removeModifier)
        }
    }
}
