package com.mineinabyss.features.quests

import com.mineinabyss.features.goals.ConditionProgress
import com.mineinabyss.features.goals.repository.GoalRepository
import com.mineinabyss.features.helpers.luckPerms
import com.mineinabyss.geary.papermc.spawning.locations.RegionService
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.Services
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.luckperms.api.node.Node
import org.bukkit.entity.Player

class QuestManager(
    val config: QuestConfig,
    val repository: GoalRepository,
) {
    fun activeQuests(player: Player): Set<String> =
        repository.allProgress(player).filterValues { !it.rewardClaimed }.keys

    fun completedQuests(player: Player): Set<String> =
        repository.allProgress(player).filterValues { it.rewardClaimed }.keys

    fun playerHasUnlockedQuest(player: Player, questId: String): Boolean {
        val progress = repository.progress(player, questId) ?: return false
        return !progress.rewardClaimed
    }

    fun playerHasCompletedQuest(player: Player, questId: String): Boolean =
        repository.progress(player, questId)?.rewardClaimed == true

    fun isQuestCompleted(player: Player, questId: String): Boolean =
        repository.progress(player, questId)?.completed == true

    fun unlockQuest(player: Player, questId: String) {
        val quest = config.byId(questId) ?: return player.error("Quest $questId not found")
        val progress = repository.progress(player, questId)
        when {
            progress?.rewardClaimed == true -> player.error("You have already completed this quest")
            progress != null -> player.error("You already have this quest active")
            else -> {
                repository.startGoal(player, quest.toGoal())
                // seed regions the player is already inside, enter events only fire on transitions
                Services.getOrNull<RegionService>()?.let {
                    repository.seedRegions(player, it.regionsAt(player.location))
                }
            }
        }
    }

    fun checkAndCompleteQuest(player: Player, questId: String) {
        val progress = repository.progress(player, questId)
        when {
            progress?.completed != true -> player.error("You haven't completed this quest yet!")
            progress.rewardClaimed -> player.error("You have already claimed this quest's reward")
            else -> claimReward(player, questId)
        }
    }

    fun completeQuest(player: Player, questId: String) {
        val quest = config.byId(questId) ?: return player.error("Quest $questId not found")
        repository.forceComplete(player, quest.toGoal())
        claimReward(player, questId)
    }

    fun claimReward(player: Player, questId: String) {
        val quest = config.byId(questId) ?: return
        quest.rewards.forEach { serializable ->
            val item = serializable.toItemStackOrNull()
                ?: return player.error("Failed to complete quest $questId: Reward not found")
            player.inventory.addItem(item)
        }
        val nodes = quest.perms.map { Node.builder(it).value(true).build() }
        if (nodes.isNotEmpty()) luckPerms.userManager.modifyUser(player.uniqueId) { lpUser ->
            nodes.forEach { lpUser.data().add(it) }
        }
        repository.markRewardClaimed(player, quest.toGoal())
        player.success("Quest completed: ${quest.name}")
    }

    fun questInformation(player: Player, questId: String): TextComponent {
        val quest = config.byId(questId) ?: return Component.text(questId)
        val progress = repository.progress(player, questId)
            ?: return Component.text(""""${quest.name}" - not started.""")
        val conditions = quest.conditions.mapIndexed { i, condition ->
            condition.describe(progress.conditions[i] ?: ConditionProgress())
        }
        return Component.text(""""${quest.name}" - ${conditions.joinToString("; ")}""")
    }

    fun removeQuest(player: Player, questId: String) {
        val quest = config.byId(questId) ?: return player.error("Quest $questId not found")
        repository.removeGoal(player, quest.toGoal())
    }

    fun resetQuest(player: Player, questId: String) {
        val quest = config.byId(questId) ?: return player.error("Quest $questId not found")
        repository.resetGoal(player, quest.toGoal())
    }

    fun clearQuests(player: Player) {
        config.quests.forEach { repository.removeGoal(player, it.toGoal()) }
    }
}
