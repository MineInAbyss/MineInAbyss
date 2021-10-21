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
    fun EntityDamageByEntityEvent.onFrostAspectHit() {//
        val damager = damager as Player
        val item = damager.inventory.itemInMainHand
        val length = TimeSpan(80000)
        val damageTimer = TimeSpan(20000)
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
                    } while ((freezeTimePassed < damageTimer.inSeconds) && ((entity.freezeTicks == entity.maxFreezeTicks) || (entity.freezeTicks in (1 until entity.maxFreezeTicks))))

                    freezeTimePassed = 0L
                    (entity as LivingEntity).damage(2.0)
                    timePassed += System.currentTimeMillis() - lastTime
                    if ((entity as LivingEntity).health == 0.0) timePassed = length.inSeconds.toLong()
                } while (timePassed < length.inSeconds)
            }
        }
    }
}