package com.derongan.minecraft.mineinabyss.ascension.effect.effects

import com.derongan.minecraft.mineinabyss.ascension.effect.AbstractAscensionEffect
import com.derongan.minecraft.mineinabyss.util.TickUtils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Serializable
@SerialName("potion")
data class PotionAscensionEffect(
        val strength: Int = 1,
        override val offset: Long = 0,
        override val iterations: Int = 1,
        @SerialName("duration")
        val secDuration: Int,
        @SerialName("effects")
        private val _effectsToApply: List<String>
) : AbstractAscensionEffect() {
    @Transient
    override val duration = TickUtils.milisecondsToTicks(secDuration * 1000)

    @Transient
    val effectsToApply = _effectsToApply.mapNotNull { PotionEffectType.getByName(it) }

    override fun applyEffect(player: Player) {
        for (potionEffectType in effectsToApply) {
            val totalDuration = (player.getPotionEffect(potionEffectType)?.duration ?: 0) + duration
            player.addPotionEffect(PotionEffect(potionEffectType, totalDuration, strength))
        }
    }

    override fun clone() = copy()

    //TODO make potion effects be able to merge in different ways

    /* public void mergeAddPotionEffect(Player player, PotionEffectType potionEffect, int newEffectDuration, int newStrength) {
     if (player.getPotionEffect(potionEffect).getDuration() < newEffectDuration) {
     } else if (player.getPotionEffect(potionEffect).getDuration() > newEffectDuration) {
     } else {}
     } */

    /*fun mergeExtendPotionEffect(player: Player, potionEffect: PotionEffectType?, newPotionEffectDuration: Int) {
        val extendedPotionEffect = PotionEffect(potionEffect!!, newPotionEffectDuration + player.getPotionEffect(potionEffect)!!.duration, strength)
        player.removePotionEffect(potionEffect)
        player.addPotionEffect(extendedPotionEffect)
    }*/
}