package com.mineinabyss.features.helpers

import com.mineinabyss.features.overlay.Emotes
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

private val EMOTE = Regex(":([a-z0-9_]+):")
// Longer than anything shown under a held title, releaseFadeTitle replaces it to fade out
private val TITLE_HOLD = 1.hours

/** A [title] of only `:<emote>:` shows that Emojy emote, anything else is MiniMessage */
fun Player.showFadeTitle(title: String, fadeIn: Duration, stay: Duration, fadeOut: Duration) {
    val text = EMOTE.matchEntire(title)?.let { Emotes.find(it.groupValues[1]) }?.let(Emotes::component) ?: title.miniMsg()
    showTitle(Title.title(text, Component.empty(), Title.Times.times(fadeIn.toJavaDuration(), stay.toJavaDuration(), fadeOut.toJavaDuration())))
}

fun Player.holdFadeTitle(title: String, fadeIn: Duration) = showFadeTitle(title, fadeIn, TITLE_HOLD, Duration.ZERO)

fun Player.releaseFadeTitle(title: String, fadeOut: Duration) = showFadeTitle(title, Duration.ZERO, Duration.ZERO, fadeOut)

fun postEffectSteps(base: Key, steps: Int): List<Key> = (1..steps).map { Key.key(base.namespace(), "${base.value()}_$it") }

/** Shows [step] and removes the other [steps], a null [step] removes them all */
fun Player.showPostEffectStep(steps: List<Key>, step: Key?) {
    val effects = postEffects()
    steps.filter { it != step && it in effects.values() }.forEach(effects::remove)
    if (step != null && step !in effects.values()) effects.add(step)
}

/** Swaps through [steps] in order when [on], or backwards to none, spread over [duration] */
suspend fun Player.slidePostEffects(steps: List<Key>, on: Boolean, duration: Duration, wait: suspend (Duration) -> Unit) {
    val order = if (on) steps else steps.reversed().drop(1) + null
    order.forEach { step ->
        showPostEffectStep(steps, step)
        wait(duration / order.size)
    }
}

fun Player.removePostEffects(prefixes: Collection<String>) = postEffects().run {
    values().filter { key -> key.namespace() == "mineinabyss" && prefixes.any { key.value().startsWith(it) } }.forEach(::remove)
}
