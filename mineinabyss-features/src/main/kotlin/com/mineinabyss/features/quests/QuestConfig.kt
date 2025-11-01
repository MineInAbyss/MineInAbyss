package com.mineinabyss.features.quests

import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.LocationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
data class VisitQuest(
    val questId: String,
    val locations: List<LocationData>,
    val gearyRewards: Map<PrefabKey, Int>? = null,
    val vanillaRewards: Map<String, Int>? = null,
    val perms: List<String>? = null,
) {

}

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