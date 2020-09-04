package com.derongan.minecraft.mineinabyss.ascension.effect

import com.derongan.minecraft.mineinabyss.ascension.effect.effects.DeathAscensionEffect
import com.derongan.minecraft.mineinabyss.ascension.effect.effects.ParticleAscensionEffect
import com.derongan.minecraft.mineinabyss.ascension.effect.effects.PotionAscensionEffect
import com.derongan.minecraft.mineinabyss.ascension.effect.effects.SoundAscensionEffect
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

object EffectRegistration {
    val module = SerializersModule {
        polymorphic(AscensionEffect::class) {
            subclass(DeathAscensionEffect.serializer())
            subclass(ParticleAscensionEffect.serializer())
            subclass(PotionAscensionEffect.serializer())
            subclass(SoundAscensionEffect.serializer())
        }
    }
}