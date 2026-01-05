package com.mineinabyss.features.quests

import com.mineinabyss.geary.prefabs.PrefabKey
import kotlinx.serialization.Serializable

@Serializable
data class VisitQuest(
    val questId: String,
    val displayName: String,
    val locations: List<LocationData>,
    val gearyRewards: Map<PrefabKey, Int> = emptyMap(),
    val vanillaRewards: Map<String, Int> = emptyMap(),
    val perms: List<String> = emptyList(),
)

@Serializable

class FetchQuest {
}

@Serializable
class KillQuest {
}

@Serializable
class QuestConfig(
    val visitQuests: Map<String, VisitQuest> = emptyMap(), // <QuestID, VisitQuest>
    val fetchQuests: Map<String, FetchQuest> = emptyMap(),
    val killQuests: Map<String,KillQuest> = emptyMap(),
) {

}