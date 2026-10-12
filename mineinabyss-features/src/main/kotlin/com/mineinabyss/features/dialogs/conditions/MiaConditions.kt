package com.mineinabyss.features.dialogs.conditions

import com.mineinabyss.features.dialogs.runtime.player
import com.mineinabyss.features.dialogs.runtime.unlocks
import com.mineinabyss.features.helpers.Cost
import com.mineinabyss.features.quests.questManager
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:permission")
@JvmInline
value class PermissionCondition(val permission: String) : Condition {
    override fun ActionGroupContext.execute(): Boolean = player?.hasPermission(permission) == true
}

@Serializable
@SerialName("mineinabyss:can_afford")
@JvmInline
value class CanAffordCondition(val cost: Cost) : Condition {
    override fun ActionGroupContext.execute(): Boolean = player?.let(cost::canAfford) == true
}

enum class QuestState {
    NOT_STARTED, ACTIVE, COMPLETED, CLAIMED
}

/** [QuestState.COMPLETED] means the objective is done but the reward is not claimed yet */
@Serializable
@SerialName("mineinabyss:quest")
class QuestCondition(val id: String, val state: QuestState = QuestState.ACTIVE) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val player = player ?: return false
        val quests = questManager ?: return false
        val current = when {
            quests.playerHasCompletedQuest(player, id) -> QuestState.CLAIMED
            quests.isQuestCompleted(player, id) -> QuestState.COMPLETED
            quests.playerHasUnlockedQuest(player, id) -> QuestState.ACTIVE
            else -> QuestState.NOT_STARTED
        }
        return current == state
    }
}

@Serializable
@SerialName("mineinabyss:unlocked")
@JvmInline
value class UnlockedCondition(val key: String) : Condition {
    override fun ActionGroupContext.execute(): Boolean = player?.unlocks?.contains(key) == true
}

enum class WhistleRank {
    BELL, RED, BLUE, MOON, BLACK, WHITE
}

// Placeholder until whistles are done, a rank is held through the permission mineinabyss.whistle.<rank>
@Serializable
@SerialName("mineinabyss:whistle_rank")
@JvmInline
value class WhistleRankCondition(val atLeast: WhistleRank) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val player = player ?: return false
        return WhistleRank.entries.drop(atLeast.ordinal).any { player.hasPermission("mineinabyss.whistle.${it.name.lowercase()}") }
    }
}
