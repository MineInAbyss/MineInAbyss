package com.derongan.minecraft.mineinabyss.ascension.effect

import com.derongan.minecraft.mineinabyss.ascension.effect.effects.*
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
            subclass(MaxHealthChangeEffect.serializer())
            subclass(HallucinatingAscensionEffect.serializer())
        }
    }
}