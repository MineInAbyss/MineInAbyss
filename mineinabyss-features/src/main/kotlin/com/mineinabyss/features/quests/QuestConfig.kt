package com.mineinabyss.features.quests

import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.KeySerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.serialization.SingleOrListSerializer
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import kotlin.jvm.java

@Serializable
data class VisitQuest(
    val questId: String,
    val displayName: String,
    val locations: @Serializable(SingleOrListSerializer::class) List<LocationData>,
    val rewards: @Serializable(SingleOrListSerializer::class) List<SerializableItemStack> = emptyList(),
    val perms: @Serializable(SingleOrListSerializer::class) List<String> = emptyList(),
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