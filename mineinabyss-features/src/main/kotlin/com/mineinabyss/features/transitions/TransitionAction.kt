package com.mineinabyss.features.transitions

import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.player
import com.mineinabyss.features.helpers.runIn
import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Tasks
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Runs [actions] while the screen is dark */
@Serializable
@SerialName("mineinabyss:transition")
class TransitionAction(
    val transition: TransitionSource,
    val actions: Tasks,
) : Action {
    override fun ActionGroupContext.execute() {
        val player = player ?: return
        val transitions = transitions ?: return abyss.logger.w("Transitions feature is not loaded, cannot play transition $transition")
        val resolved = transition.resolve() ?: return abyss.logger.w("Unknown transition $transition")
        val context = this
        transitions.play(player, resolved) {
            actions.runIn(context)
            true
        }
    }
}
