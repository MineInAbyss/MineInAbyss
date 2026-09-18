package com.mineinabyss.features.goals.goalListener

import com.mineinabyss.features.goals.FactKind
import com.mineinabyss.features.goals.repository.GoalRepository
import com.mineinabyss.staminaclimb.events.PlayerStartClimbEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ClimbFactListener(private val repository: GoalRepository) : Listener {
    @EventHandler
    fun PlayerStartClimbEvent.onClimb() {
        repository.recordFact(player, FactKind.CLIMB)
    }
}
