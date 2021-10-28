package com.derongan.minecraft.mineinabyss.systems

import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.mineinabyss.idofront.time.TimeSpan
import com.okkero.skedule.schedule
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

object FrostAspectListener : Listener {

    @EventHandler
    fun EntityDamageByEntityEvent.onFrostAspectHit() {
        val damager = damager as? Player ?: return
        val item = damager.inventory.itemInMainHand
        val enchantLvl = item.getEnchantmentLevel(CustomEnchants.FROST_ASPECT)
        val length = TimeSpan(10000).inSeconds.times(enchantLvl) // Multiplied duration by level.
        val damageTimer = TimeSpan(10000).inSeconds
        var timePassed = 0L
        var freezeTimePassed = 0L
        val lastTime = System.currentTimeMillis()

        if (item.containsEnchantment(CustomEnchants.FROST_ASPECT)) {
            mineInAbyss.schedule {
                do {
                    val lastFreezeTime = System.currentTimeMillis()
                    do {
                        entity.freezeTicks = entity.maxFreezeTicks
                        freezeTimePassed += System.currentTimeMillis() - lastFreezeTime
                        waitFor(1)
                    } while ((freezeTimePassed < damageTimer) &&
                        ((entity.freezeTicks == entity.maxFreezeTicks) ||
                                (entity.freezeTicks in (1 until entity.maxFreezeTicks)))
                    )

                    freezeTimePassed = 0L
                    (entity as LivingEntity).damage(1.0)
                    entity.freezeTicks = entity.maxFreezeTicks
                    timePassed += System.currentTimeMillis() - lastTime
                    if ((entity as LivingEntity).health == 0.0) timePassed = length.toLong()
                } while (timePassed < length)
            }
        }
    }
}