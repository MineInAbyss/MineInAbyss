package com.mineinabyss.features.cinematics

import com.mineinabyss.geary.actions.Tasks
import com.mineinabyss.geary.autoscan.ExcludeAutoScan
import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.serialization.KeySerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kyori.adventure.key.Key
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/** Steps run in order, a step marked [CinematicStep.parallel] lets the next one start right away */
@Serializable
class CinematicScene(
    val steps: List<CinematicStep>,
    /** Run after the scene ends normally, not when it is cancelled */
    val onEnd: Tasks? = null,
    /** Lets the player sneak to skip, the screen fades out while the rest of the scene plays out instantly, then fades back in */
    val skippable: Boolean = true,
    val skipFade: SkipFade = SkipFade(),
    /** Hides the player from everyone else while the scene plays */
    val hidePlayer: Boolean = true,
    /** A mannequin the player takes the place of when the scene ends, otherwise they go back to where they started */
    val endAt: String? = null,
)

/** [title] is MiniMessage or an Emojy emote written as `:id:`, shown over the screen while a skipped scene catches up */
@Serializable
class SkipFade(
    val title: String = ":screen_fade:",
    val fadeIn: @Serializable(DurationSerializer::class) Duration = 500.milliseconds,
    val fadeOut: @Serializable(DurationSerializer::class) Duration = 800.milliseconds,
)

/**
 * Written as `[x, y, z]` or `[x, y, z, yaw, pitch]` relative to the scene's anchor.
 * The offset turns with the anchor's yaw, so +z is in front of the anchor
 */
@Serializable(with = ScenePosition.Serializer::class)
data class ScenePosition(val x: Double, val y: Double, val z: Double, val yaw: Float? = null, val pitch: Float? = null) {
    object Serializer : KSerializer<ScenePosition> {
        private val list = ListSerializer(Double.serializer())
        override val descriptor = list.descriptor

        override fun deserialize(decoder: Decoder): ScenePosition {
            val values = list.deserialize(decoder)
            require(values.size == 3 || values.size == 5) { "A position needs [x, y, z] or [x, y, z, yaw, pitch], got $values" }
            return ScenePosition(values[0], values[1], values[2], values.getOrNull(3)?.toFloat(), values.getOrNull(4)?.toFloat())
        }

        override fun serialize(encoder: Encoder, value: ScenePosition) =
            list.serialize(encoder, listOfNotNull(value.x, value.y, value.z, value.yaw?.toDouble(), value.pitch?.toDouble()))
    }
}

enum class Ease {
    LINEAR, IN, OUT, IN_OUT;

    fun apply(t: Double): Double = when (this) {
        LINEAR -> t
        IN -> t * t
        OUT -> 1 - (1 - t) * (1 - t)
        IN_OUT -> t * t * (3 - 2 * t)
    }
}

@Serializable
@ExcludeAutoScan
sealed interface CinematicStep {
    val parallel: Boolean
}

@Serializable
@ExcludeAutoScan
@SerialName("WAIT")
class WaitStep(
    val duration: @Serializable(DurationSerializer::class) Duration,
    override val parallel: Boolean = false,
) : CinematicStep

/** Slides letterbox bars in or out */
@Serializable
@ExcludeAutoScan
@SerialName("BARS")
class BarsStep(
    val show: Boolean = true,
    val duration: @Serializable(DurationSerializer::class) Duration = 500.milliseconds,
    override val parallel: Boolean = false,
) : CinematicStep

/** Fades to or from black through the steps of a post effect, see `WarpTransition.postEffectSteps` */
@Serializable
@ExcludeAutoScan
@SerialName("FADE")
class FadeStep(
    val darken: Boolean = true,
    val duration: @Serializable(DurationSerializer::class) Duration = 500.milliseconds,
    val effect: @Serializable(KeySerializer::class) Key = DEFAULT_FADE,
    val effectSteps: Int = 10,
    override val parallel: Boolean = false,
) : CinematicStep

internal val DEFAULT_FADE = Key.key("mineinabyss", "gondola/fade")

/**
 * A stand-in for the player, wearing their skin and armor unless [copyPlayer] is false.
 * Without [at] it stands where the player is, looking where they look, so following it straight away is a seamless cut to first person
 */
@Serializable
@ExcludeAutoScan
@SerialName("MANNEQUIN")
class MannequinStep(
    val id: String,
    val at: ScenePosition? = null,
    val copyPlayer: Boolean = true,
    override val parallel: Boolean = false,
) : CinematicStep

/** Walks a mannequin through [path] at [speed] blocks per second, a player walks at about 4.3 */
@Serializable
@ExcludeAutoScan
@SerialName("WALK")
class WalkStep(
    val id: String,
    val path: List<ScenePosition>,
    val speed: Double = 3.0,
    /** Keeps the mannequin on blocks within a step of the path, so the path only needs points where it turns */
    val snapToGround: Boolean = true,
    override val parallel: Boolean = false,
) : CinematicStep {
    init {
        require(speed > 0) { "A walk needs a speed above 0, got $speed" }
    }
}

/**
 * Cuts to [at], or moves along [path] over [duration].
 * [follow] views through a mannequin's eyes instead.
 * [lookAt] is a position or a mannequin or gondola id the camera keeps facing, otherwise the camera uses the yaw and pitch of each point
 */
@Serializable
@ExcludeAutoScan
@SerialName("CAMERA")
class CameraStep(
    val at: ScenePosition? = null,
    val path: List<ScenePosition> = emptyList(),
    val duration: @Serializable(DurationSerializer::class) Duration = 2.seconds,
    val ease: Ease = Ease.IN_OUT,
    val follow: String? = null,
    val lookAt: String? = null,
    val lookAtPosition: ScenePosition? = null,
    override val parallel: Boolean = false,
) : CinematicStep

/**
 * Spawns a ModelEngine [model] at [at] if [id] does not exist yet, then moves it along [path] over [duration], or drops it when [fall] is set.
 * Mannequins in [carry] ride it from its center, standing on its floor
 */
@Serializable
@ExcludeAutoScan
@SerialName("GONDOLA")
class GondolaStep(
    val id: String,
    val model: String = "gondola",
    val at: ScenePosition? = null,
    val path: List<ScenePosition> = emptyList(),
    val duration: @Serializable(DurationSerializer::class) Duration = 5.seconds,
    val ease: Ease = Ease.IN_OUT,
    val animation: String? = null,
    val carry: List<String> = emptyList(),
    /** Blocks between the model's origin and the top of its floor */
    val floor: Double = 1.5,
    /** Drops until blocks are under it instead of following [path], moving to the next DeeperWorld section on the way */
    val fall: Boolean = false,
    /** Top speed of [fall] in blocks per second */
    val speed: Double = 20.0,
    /** Blocks per second gained each second while falling */
    val acceleration: Double = 6.0,
    val landSound: String? = "minecraft:block.wood.fall",
    /**
     * Puts the player themselves in the middle of the floor and views through their own eyes, so they can look around.
     * The mannequin being viewed through is removed once the view is back on the player, and the player stays where the ride ends
     */
    val carryPlayer: Boolean = false,
    override val parallel: Boolean = false,
) : CinematicStep {
    init {
        require(!fall || (speed > 0 && acceleration > 0)) { "A falling gondola needs a speed and acceleration above 0, got $speed and $acceleration" }
    }
}

/** Removes a mannequin or gondola */
@Serializable
@ExcludeAutoScan
@SerialName("REMOVE")
class RemoveStep(
    val id: String,
    override val parallel: Boolean = false,
) : CinematicStep

@Serializable
@ExcludeAutoScan
@SerialName("SOUND")
class SoundStep(
    val sound: String,
    val volume: Float = 1f,
    val pitch: Float = 1f,
    override val parallel: Boolean = false,
) : CinematicStep

/** Runs Geary actions for the player, like dialog actions */
@Serializable
@ExcludeAutoScan
@SerialName("ACTIONS")
class ActionsStep(
    val actions: Tasks,
    override val parallel: Boolean = false,
) : CinematicStep
