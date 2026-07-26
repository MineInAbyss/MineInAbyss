package com.mineinabyss.features.achievements.data

import com.mineinabyss.features.goals.dataStore.GoalProgressStore

// our costom achievements
object AchievementStore : GoalProgressStore("achievements")

// vanilla system
object AdvancementStore : GoalProgressStore("advancements")
