package com.derongan.minecraft.mineinabyss.player

import com.derongan.minecraft.mineinabyss.MineInAbyss.Companion.econ
import com.derongan.minecraft.mineinabyss.ascension.effect.effects.MaxHealthChangeEffect
import com.derongan.minecraft.mineinabyss.ecs.components.playerData
import com.derongan.minecraft.mineinabyss.ecs.systems.OrthReturnSystem
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerExpChangeEvent
import org.bukkit.event.player.PlayerQuitEvent

object PlayerListener : Listener {
    @EventHandler
    fun onPlayerLeave(playerQuitEvent: PlayerQuitEvent) {
        val (player) = playerQuitEvent
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.run {
            modifiers.filter {
                it.name == MaxHealthChangeEffect.CURSE_MAX_HEALTH
                        && !MaxHealthChangeEffect.activeEffects.contains(it)
            }.forEach {
                removeModifier(it)
            }
        }
    }

    @EventHandler
    fun onPlayerDeath(pde: PlayerDeathEvent) {
        val player = pde.entity

        //TODO maybe limit this to only the survival server with a config option
        if (player.lastDamageCause?.cause == EntityDamageEvent.DamageCause.VOID) pde.keepInventory = true

        OrthReturnSystem.removeDescentContext(player)
    }

    @EventHandler
    fun onPlayerGainEXP(e: PlayerExpChangeEvent) {
        val (player, amount) = e
        if (amount <= 0) return
        econ?.depositPlayer(player, amount.toDouble())
        player.playerData.addExp(amount.toDouble())
    }

}
