package com.mineinabyss.features.locations

import com.mineinabyss.features.abyss
import com.mineinabyss.features.dialogs.dialogSessions
import com.mineinabyss.features.helpers.OffsetArea
import com.mineinabyss.features.helpers.OffsetAreaSerializer
import com.mineinabyss.features.helpers.ScalarShorthandSerializer
import com.mineinabyss.features.helpers.isInCinematicOrTransition
import com.mineinabyss.features.helpers.player
import com.mineinabyss.features.transitions.TransitionSource
import com.mineinabyss.features.transitions.isInTransitionDark
import com.mineinabyss.features.transitions.resolve
import com.mineinabyss.features.transitions.transitions
import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.autoscan.ExcludeAutoScan
import com.mineinabyss.idofront.messaging.error
import kotlinx.coroutines.future.await
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param transition left out teleports straight away
 * @param offset an area around [to] the player lands at random inside of
 */
@Serializable
@ExcludeAutoScan
class WarpTo(
    val to: LocationPoint,
    val transition: TransitionSource? = null,
    val offset: @Serializable(OffsetAreaSerializer::class) OffsetArea = OffsetArea.NONE,
) {
    object Shorthand : ScalarShorthandSerializer<WarpTo>(serializer(), { WarpTo(LocationPointSerializer.deserialize(it)) })
}

/** Written as just a location point, or as a map of [WarpTo] */
@Serializable
@SerialName("mineinabyss:warp_to")
@JvmInline
value class WarpToAction(val warp: @Serializable(WarpTo.Shorthand::class) WarpTo) : Action {
    override fun ActionGroupContext.execute() {
        val player = player ?: return
        val inDark = player.isInTransitionDark()
        if (!inDark && player.isInCinematicOrTransition())
            return abyss.logger.w("warp_to ${warp.to} refused for ${player.name}, they are in a transition or cinematic")
        val landing = warp.offset.randomIn(warp.to.resolve())

        if (inDark && warp.transition != null)
            abyss.logger.w("warp_to ${warp.to} runs inside another transition, teleporting without its own")
        val transition = if (inDark) null else warp.transition?.let { source ->
            source.resolve() ?: null.also { abyss.logger.w("Unknown transition $source in warp_to ${warp.to}, teleporting without it") }
        }
        val transitions = transition?.let {
            transitions ?: null.also { abyss.logger.w("Transitions feature is not loaded, warp_to ${warp.to} teleports without its transition") }
        }
        if (transition == null || transitions == null) {
            dialogSessions?.close(player)
            player.teleportAsync(landing)
            return
        }

        transitions.play(player, transition) {
            player.teleportAsync(landing).await().also { teleported ->
                if (!teleported) player.error("The trip was called off")
            }
        }
    }
}
