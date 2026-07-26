package com.mineinabyss.features.goals

import com.mineinabyss.features.goals.dataStore.GoalProgress
import kotlinx.serialization.Serializable

@Serializable
data class Goal(
    // Goal info
    val id: String,
    val name: String,
    val description: String = "",

    // Goal content
    val conditions: List<GoalCondition> = emptyList(),
) {

    fun validate(progress: GoalProgress): Boolean = conditions.withIndex().all { (i, condition) ->
        condition.isMet(progress.conditions[i] ?: ConditionProgress())
    }

    fun tracksAny(kind: FactKind, value: String) = conditions.any { it.tracks(kind, value) }
}

@Serializable
data class GoalsConfig(
    val goals: List<Goal> = emptyList(),
) {
    fun byId(id: String): Goal? { return goals.find { it.id == id } }
}
