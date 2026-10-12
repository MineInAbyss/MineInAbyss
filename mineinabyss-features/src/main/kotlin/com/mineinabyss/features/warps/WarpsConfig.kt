package com.mineinabyss.features.warps

import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlScalar
import com.mineinabyss.features.helpers.postEffectSteps
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.serialization.KeySerializer
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kyori.adventure.key.Key
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Serializable
data class WarpsConfig(
    /** Nexo block placed on the stops of warps with `triggers: true` */
    val triggerBlock: PrefabKey = PrefabKey.of("mineinabyss", "gondola_trigger"),
    val defaultDialog: PrefabKey = PrefabKey.of("mineinabyss", "warp_ride"),
    val transitions: Map<String, WarpTransition> = mapOf(DEFAULT_TRANSITION to WarpTransition()),
) {
    private val defaultTransition get() = transitions[DEFAULT_TRANSITION] ?: WarpTransition()

    fun transition(ref: TransitionRef?): WarpTransition = when (ref) {
        null -> defaultTransition
        is TransitionRef.Inline -> ref.transition
        is TransitionRef.Named -> transitions[ref.name] ?: defaultTransition
    }

    companion object {
        const val DEFAULT_TRANSITION = "default"
    }
}

/** Fades in over [fadeIn], waits [travelTime], teleports, then fades out over [fadeOut], [loopSound] plays from the start of the fade until the teleport */
@Serializable
data class WarpTransition(
    val postEffect: @Serializable(KeySerializer::class) Key? = null,
    /** Above 1, [postEffect] names a set of effects `<postEffect>_1` to `<postEffect>_<steps>` that get darker and are swapped through to fade */
    val postEffectSteps: Int = 1,
    val title: String? = null,
    val loopSound: @Serializable(KeySerializer::class) Key? = null,
    val loopSoundInterval: @Serializable(DurationSerializer::class) Duration = 500.milliseconds,
    val arriveSound: @Serializable(KeySerializer::class) Key? = null,
    val volume: Float = 0.6f,
    val fadeIn: @Serializable(DurationSerializer::class) Duration = 500.milliseconds,
    val travelTime: @Serializable(DurationSerializer::class) Duration = Duration.ZERO,
    val fadeOut: @Serializable(DurationSerializer::class) Duration = 500.milliseconds,
    val freeze: Boolean = true,
) {
    fun fadeEffects(): List<Key> {
        val effect = postEffect ?: return emptyList()
        if (postEffectSteps <= 1) return listOf(effect)
        return postEffectSteps(effect, postEffectSteps)
    }
}

/** A transition name from the warps config, or a transition written in place */
@Serializable(with = TransitionRef.Serializer::class)
sealed interface TransitionRef {
    class Named(val name: String) : TransitionRef
    class Inline(val transition: WarpTransition) : TransitionRef

    object Serializer : KSerializer<TransitionRef> {
        override val descriptor: SerialDescriptor = ContextualSerializer(Any::class).descriptor

        override fun deserialize(decoder: Decoder): TransitionRef {
            val node = (decoder as? YamlInput)?.node
            if (node == null || node is YamlScalar) return Named(decoder.decodeString())
            return Inline(decoder.decodeSerializableValue(WarpTransition.serializer()))
        }

        override fun serialize(encoder: Encoder, value: TransitionRef) = when (value) {
            is Named -> encoder.encodeString(value.name)
            is Inline -> encoder.encodeSerializableValue(WarpTransition.serializer(), value.transition)
        }
    }
}
