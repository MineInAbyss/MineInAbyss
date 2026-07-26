package com.mineinabyss.features.achievements

import com.mineinabyss.features.goals.Goal
import com.mineinabyss.features.goals.GoalCondition
import kotlinx.serialization.Serializable


@Serializable
data class Achievement(
    val id: String,
    val name: String,
    val description: String = "",
    val requires: List<String> = emptyList(),
    val conditions: List<GoalCondition> = emptyList(),
) {
    fun toGoal() = Goal(id, name, description, conditions)

    fun isUnlockedBy(completedIds: Set<String>) = completedIds.containsAll(requires)
}

@Serializable
data class AchievementsConfig(
    val achievements: List<Achievement> = emptyList(),
) {
    fun byId(id: String): Achievement? { return achievements.find { it.id == id } }
}
