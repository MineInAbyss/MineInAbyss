package com.mineinabyss.features.mobhealth

import com.mineinabyss.components.mobs.ShowHealthBar
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.time.inWholeTicks
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import java.util.*

class MobHealthBars(private val config: MobHealthConfig) : Listener {
    private val glyphs = BarGlyphs(config)
    private val bars = Object2ObjectOpenHashMap<UUID, HealthBar>()

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun EntityDamageEvent.onDamage() {
        val mob = entity as? LivingEntity ?: return
        val player = damageSource.causingEntity as? Player ?: return
        if (mob.toGearyOrNull()?.has<ShowHealthBar>() != true) return

        val maxHealth = mob.getAttribute(Attribute.MAX_HEALTH)?.value ?: mob.health
        bars.getOrPut(mob.uniqueId) { track(mob) }.hit(player, (mob.health - finalDamage) / maxHealth)
    }

    fun clear() {
        bars.values.forEach(HealthBar::remove)
        bars.clear()
    }

    private fun track(mob: LivingEntity): HealthBar {
        val bar = HealthBar(abyss, glyphs, config.visibleDuration.inWholeTicks.toInt(), mob)
        val untrack = {
            bar.remove()
            bars.remove(mob.uniqueId, bar)
        }
        mob.scheduler.runAtFixedRate(abyss, { task ->
            if (!bar.tick()) {
                untrack()
                task.cancel()
            }
        }, { untrack() }, 1L, 1L)
        return bar
    }
}
