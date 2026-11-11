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
import com.mineinabyss.idofront.messaging.error
import kotlinx.coroutines.future.await
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable

/**
 * Written as just a location point, or as a map of its fields
 * @param transition left out teleports straight away
 * @param offset an area around [to] the player lands at random inside of
 */
@KeepGeneratedSerializer
@Serializable(WarpToAction.Serializer::class)
class WarpToAction(
    val to: LocationPoint,
    val transition: TransitionSource? = null,
    val offset: @Serializable(OffsetAreaSerializer::class) OffsetArea = OffsetArea.NONE,
) : Action {
    override fun ActionGroupContext.execute() {
        val player = player ?: return
        val inDark = player.isInTransitionDark()
        if (!inDark && player.isInCinematicOrTransition())
            return abyss.logger.w("warp_to $to refused for ${player.name}, they are in a transition or cinematic")
        val landing = offset.randomIn(to.resolve())

        if (inDark && transition != null)
            abyss.logger.w("warp_to $to runs inside another transition, teleporting without its own")
        val resolved = if (inDark) null else transition?.let { source ->
            source.resolve() ?: null.also { abyss.logger.w("Unknown transition $source in warp_to $to, teleporting without it") }
        }
        val transitions = resolved?.let {
            transitions ?: null.also { abyss.logger.w("Transitions feature is not loaded, warp_to $to teleports without its transition") }
        }
        if (resolved == null || transitions == null) {
            dialogSessions?.close(player)
            player.teleportAsync(landing)
            return
        }

        transitions.play(player, resolved) {
            player.teleportAsync(landing).await().also { teleported ->
                if (!teleported) player.error("The trip was called off")
            }
        }
    }

    object Serializer : ScalarShorthandSerializer<WarpToAction>(
        generatedSerializer(), { WarpToAction(LocationPointSerializer.deserialize(it)) }, "mineinabyss:warp_to"
    )
}
