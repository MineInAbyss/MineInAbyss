package com.mineinabyss.features.quests

import com.mineinabyss.features.goals.Goal
import com.mineinabyss.features.goals.GoalCondition
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.Serializable

@Serializable
data class Quest(
    val id: String,
    val name: String,
    val description: String = "",
    val conditions: List<GoalCondition> = emptyList(),
    val rewards: List<SerializableItemStack> = emptyList(),
    val perms: List<String> = emptyList(),
) {
    fun toGoal() = Goal(id, name, description, conditions)
}

@Serializable
data class QuestConfig(
    val quests: List<Quest> = emptyList(),
) {
    fun byId(id: String): Quest? = quests.find { it.id == id }
}
