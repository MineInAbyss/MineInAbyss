package com.mineinabyss.features.achievements.data

import com.mineinabyss.idofront.datastore.KeyedDataStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonObject

@Serializable
data class AchievementProgress(
    val completed: Boolean,
    val extras: JsonObject = JsonObject(emptyMap()),
)

object AchievementStore : KeyedDataStore<String, AchievementProgress>(
    "achievements", String.serializer(), AchievementProgress.serializer()
)