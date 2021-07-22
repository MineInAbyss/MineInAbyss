package com.derongan.minecraft.mineinabyss.ecs.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

class ApplyRandomPotionEffectAction(
    private val minDuration: Int,
    private val maxDuration: Int,
    private val minAmplifier: Int,
    private val maxAmplifier: Int
) : GearyAction() {
    val GearyEntity.entity by get<LivingEntity>()

    override fun GearyEntity.run(): Boolean {
        val potentialTypes = PotionEffectType.values()
        val type = potentialTypes[Random.nextInt(0, potentialTypes.size - 1)]


        entity.addPotionEffect(
            PotionEffect(
                type,
                Random.nextInt(minDuration, maxDuration),
                Random.nextInt(minAmplifier, maxAmplifier)
            )
        )
        return true
    }
}
