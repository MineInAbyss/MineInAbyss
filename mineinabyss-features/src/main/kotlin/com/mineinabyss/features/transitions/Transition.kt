package com.mineinabyss.features.transitions

import com.mineinabyss.features.helpers.KeyOrInline
import com.mineinabyss.features.helpers.KeyOrInlineSerializer
import com.mineinabyss.features.helpers.loaded
import com.mineinabyss.features.helpers.postEffectSteps
import com.mineinabyss.geary.autoscan.ExcludeAutoScan
import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.serialization.KeySerializer
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Fades in over [fadeIn], waits [travelTime], runs what happens while the screen is dark, then fades out over [fadeOut].
 * [loopSound] plays from the start of the fade until the dark point.
 * Above 1, [postEffect] names a set of effects `<postEffect>_1` to `<postEffect>_<steps>` that get darker and are swapped through to fade
 */
@Serializable
@ExcludeAutoScan
data class Transition(
    val postEffect: @Serializable(KeySerializer::class) Key? = null,
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

typealias TransitionSource = @Serializable(TransitionSourceSerializer::class) KeyOrInline<Transition>

object TransitionSourceSerializer : KeyOrInlineSerializer<Transition>(Transition::serializer)

fun TransitionSource.resolve() = resolveWith { loaded<TransitionRegistry>(TransitionFeature)?.get(it) }
