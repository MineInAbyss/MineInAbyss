@file:UseSerializers(DurationSerializer::class)

package com.mineinabyss.features.curse.effects

import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.time.ticks
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import kotlin.math.abs
import kotlin.time.Duration

@Serializable
@SerialName("maxHealth")
data class MaxHealthChangeEffect(
    val addMaxHealth: Double,
    override val offset: Duration = 0.ticks,
    override val duration: Duration,
    override val iterations: Int = 1,
    val minHealth: Double? = null,
) : AbstractAscensionEffect() {
    @Transient var modifier: AttributeModifier? = null

    override fun applyEffect(player: Player) {
        val maxHealth = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 0.0
        //if more health than minHealth, or enough health to safely remove addMaxHealth
        if (maxHealth > (minHealth ?: abs(addMaxHealth))) {
            val newMod = AttributeModifier(CURSE_MAX_HEALTH, addMaxHealth, AttributeModifier.Operation.ADD_NUMBER)
            modifier = newMod
            activeEffects += newMod.key
            cleanUp(player)
            player.getAttribute(Attribute.MAX_HEALTH)?.addModifier(newMod)

            val (x, y, z) = player.eyeLocation
            player.spawnParticle(Particle.SOUL, x, y, z, 6, 0.2, 0.5, 0.2, 0.02)
            player.playSound(player.location, Sound.PARTICLE_SOUL_ESCAPE, 10f, 1f)
        }
    }

    override fun cleanUp(player: Player) {
        modifier?.let {
            activeEffects.remove(it.key)
            player.getAttribute(Attribute.MAX_HEALTH)?.removeModifier(it)
        }
    }

    companion object {
        val activeEffects = mutableSetOf<Key>()
        val CURSE_MAX_HEALTH = NamespacedKey.fromString("mineinabyss:curse.max_health")!!
    }

    override fun clone() = copy()
}
