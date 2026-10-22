package com.mineinabyss.features.transitions

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.features.abyss
import com.mineinabyss.features.dialogs.dialogSessions
import com.mineinabyss.features.helpers.freeze
import com.mineinabyss.features.helpers.holdFadeTitle
import com.mineinabyss.features.helpers.isInCinematicOrTransition
import com.mineinabyss.features.helpers.releaseFadeTitle
import com.mineinabyss.features.helpers.showPostEffectStep
import com.mineinabyss.features.helpers.slidePostEffects
import com.mineinabyss.features.helpers.unfreeze
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.SoundStop
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.time.Duration

private val FREEZE_KEY = NamespacedKey("mineinabyss", "transition/freeze")
private val EFFECTS_KEY = NamespacedKey("mineinabyss", "transition/post_effects")

class Transitions : Listener {
    private val playing = mutableMapOf<UUID, Job>()
    private val dark = mutableSetOf<UUID>()

    fun isInTransition(player: Player) = player.uniqueId in playing

    fun isDark(player: Player) = player.uniqueId in dark

    /**
     * @param onDark runs once the screen is dark, returning false calls the transition off so it ends without the arrive sound or fading back in from there
     */
    fun play(player: Player, transition: Transition, onDark: suspend () -> Boolean): Boolean {
        if (player.isInCinematicOrTransition()) return false
        val job = abyss.launch(start = CoroutineStart.LAZY) {
            try {
                run(player, transition, onDark)
            } finally {
                if (transition.title != null) player.clearTitle()
                player.stopLoop(transition)
                restore(player)
                playing.remove(player.uniqueId, coroutineContext.job)
            }
        }
        playing[player.uniqueId] = job
        job.start()
        return true
    }

    private suspend fun run(player: Player, transition: Transition, onDark: suspend () -> Boolean) = coroutineScope {
        // Held until the dark point has finished, then sent again to fade out from there
        transition.title?.let { player.holdFadeTitle(it, transition.fadeIn) }
        if (transition.freeze) player.freeze(FREEZE_KEY)
        transition.fadeEffects().takeIf { it.isNotEmpty() }?.let { effects ->
            player.persistentDataContainer.set(EFFECTS_KEY, PersistentDataType.LIST.strings(), effects.map { it.asString() })
        }

        val loop = transition.loopSound?.let { sound ->
            launch {
                do {
                    player.playTransitionSound(sound, transition)
                    delay(transition.loopSoundInterval)
                } while (transition.loopSoundInterval.isPositive())
            }
        }
        fade(player, transition, transition.fadeIn, darken = true)
        // A dialog that started the transition stays readable during the fade
        dialogSessions?.close(player)

        delay(transition.travelTime)
        loop?.cancel()
        player.stopLoop(transition)

        if (player.isDead) return@coroutineScope
        dark += player.uniqueId
        val proceed = try {
            onDark()
        } finally {
            dark -= player.uniqueId
        }
        if (!proceed) return@coroutineScope
        transition.arriveSound?.let { player.playTransitionSound(it, transition) }
        transition.title?.let { player.releaseFadeTitle(it, transition.fadeOut) }
        fade(player, transition, transition.fadeOut, darken = false)
    }

    private fun Player.stopLoop(transition: Transition) = transition.loopSound?.let { stopSound(SoundStop.named(it)) }

    private fun Player.playTransitionSound(sound: Key, transition: Transition) =
        playSound(Sound.sound(sound, Sound.Source.AMBIENT, transition.volume, 1f))

    private suspend fun fade(player: Player, transition: Transition, duration: Duration, darken: Boolean) {
        val steps = transition.fadeEffects().takeUnless { it.isEmpty() } ?: return delay(duration)
        player.slidePostEffects(steps, darken, duration) { delay(it) }
    }

    // Post effects are saved with the player, the marker lets a transition cut off by a crash be undone on join
    private fun restore(player: Player) {
        val data = player.persistentDataContainer
        data.get(EFFECTS_KEY, PersistentDataType.LIST.strings())?.let { effects ->
            player.showPostEffectStep(effects.mapNotNull { runCatching { Key.key(it) }.getOrNull() }, null)
            data.remove(EFFECTS_KEY)
        }
        player.unfreeze(FREEZE_KEY)
    }

    fun cancelAll() = playing.values.toList().forEach(Job::cancel)

    // The cancelled transition may only clean up once it is dispatched again, after the player has been saved
    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        playing.remove(player.uniqueId)?.cancel() ?: return
        restore(player)
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() = restore(player)
}
