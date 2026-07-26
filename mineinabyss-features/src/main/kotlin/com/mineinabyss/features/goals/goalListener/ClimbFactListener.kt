package com.mineinabyss.features.goals.goalListener

import com.mineinabyss.features.goals.FactKind
import com.mineinabyss.features.goals.repository.GoalRepository
import com.mineinabyss.staminaclimb.Events.PlayerClimbEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ClimbFactListener(private val repository: GoalRepository) : Listener {
    @EventHandler
    fun PlayerClimbEvent.onClimb() {
        repository.recordFact(player, FactKind.CLIMB)
    }
}
