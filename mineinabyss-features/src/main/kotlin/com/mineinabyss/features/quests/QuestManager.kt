package com.mineinabyss.features.quests

import com.mineinabyss.features.helpers.luckPerms
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.serialization.getOrSetPersisting
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.messaging.error
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.luckperms.api.node.Node
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Serializable
@SerialName("mineinabyss:quest_data")
data class QuestData(
    val visitedLocations: Set<String> = emptySet(),
    val completedQuests: Set<String> = emptySet(),
    val activeQuests: Set<String> = emptySet()
)


object QuestManager {


    // we use this to store a player visited location and look it up;
    // will probably be moved to database implementation later on
    private fun getQuestData(player: Player): QuestData {
        return player.toGeary().getOrSetPersisting<QuestData> { QuestData() }
    }


    private fun update(player: Player, update: (QuestData) -> QuestData) {
        val data = getQuestData(player)
        val newData = update(data)
        player.toGeary().setPersisting(newData)
    }

    fun activeQuests(player: Player): Set<String> {
        return getQuestData(player).activeQuests
    }

    fun completedQuests(player: Player): Set<String> {
        return getQuestData(player).completedQuests
    }

    fun visitedLocations(player: Player): Set<String> {
        return getQuestData(player).visitedLocations
    }

    fun questInformation(player: Player, questId: String): Component {
        val config = QuestConfigHolder.config!!
        val visitQuest = config.visitQuests[questId]?.displayName ?: return Component.text(questId)
        val progress = visitQuestProgress(player, questId)
        //TODO make work for other quest types
        return Component.text(""""$visitQuest" - ${progress.first}/${progress.second} locations visited.""")
    }

    fun addVisitedLocation(player: Player, locationName: String) {
        update(player) { data -> data.copy(visitedLocations = data.visitedLocations + locationName) }
    }
    // hopefully this auto save the changes on the player
    fun addQuest(player: Player, questId: String) {
        update(player) { data -> data.copy(activeQuests = data.activeQuests + questId) }
    }

    fun completeQuest(player: Player, questId: String) {
        update(player) { data ->
            data.copy(
                activeQuests = data.activeQuests - questId,
                completedQuests = data.completedQuests + questId
            )
        }

        // give quest reward to player
        giveQuestReward(player, questId)
    }

    fun giveQuestReward(player: Player, questId: String) {
        val gearyItems = player.world.toGeary().getAddon(ItemTracking)
        val config = QuestConfigHolder.config ?: error("Trying to complete quest $questId but QuestConfig is not initialized")
        val visitQuest = config.visitQuests[questId] ?: return

        visitQuest.gearyRewards.forEach { (item, amount) ->
            val gearyItem = gearyItems.createItem(item) ?: error("Failed to complete quest $questId: Geary prefab $item not found")
            gearyItem.amount = amount.coerceIn(1, gearyItem.maxStackSize)
            player.inventory.addItem(gearyItem)
        }

        visitQuest.vanillaRewards.forEach { (itemName, amount) ->
            val material = Material.matchMaterial(itemName) ?: error("Failed to complete quest $questId: Material $itemName not found")
            val itemStack = ItemStack(material)
            itemStack.amount = amount.coerceIn(1, itemStack.maxStackSize)
            player.inventory.addItem(itemStack)
        }

        val nodes = visitQuest.perms.map { Node.builder(it).value(true).build() }
        if (nodes.isNotEmpty()) luckPerms.userManager.modifyUser(player.uniqueId) { lpUser ->
            nodes.forEach { lpUser.data().add(it) }
        }
    }

    fun unlockQuest(player: Player, questId: String) {
        val config = QuestConfigHolder.config ?: error("Trying to unlock quest $questId but QuestConfig is not initialized")
        if (questId !in config.visitQuests.keys) {
            error("Trying to unlock quest $questId but it does not exist in the QuestConfig")
        }
        val questData = getQuestData(player)
        if (questId in questData.activeQuests) {
            error("Trying to unlock quest $questId but player already has it active")
        }
        if (questId in questData.completedQuests) {
            error("Trying to unlock quest $questId but player has already completed it")
        }
        addQuest(player, questId)
    }

    fun visitQuestProgress(player: Player, questId: String): Pair<Int, Int> {
        val config = QuestConfigHolder.config ?: error("Trying to get progress of quest $questId but QuestConfig is not initialized")
        val visitQuest = config.visitQuests[questId] ?: error("Trying to get progress of quest $questId but it does not exist in the QuestConfig")
        val questData = getQuestData(player)

        val totalLocations = visitQuest.locations.size
        if (totalLocations == 0) return 0 to 0

        val visitedLocations = visitQuest.locations.count { locationData ->
            locationData.name in questData.visitedLocations
        }

        return visitedLocations to totalLocations
    }


    fun isVisitQuestCompleted(questId: String, config: QuestConfig, questData: QuestData): Boolean {
        val visitQuest = config.visitQuests[questId] ?: return false

        return visitQuest.locations.all { locationData ->
            locationData.name in questData.visitedLocations
        }
    }

    fun isKillQuestCompleted(questId: String, config: QuestConfig, questData: QuestData): Boolean {
        // Placeholder implementation
        return false
    }

    fun isFetchQuestCompleted(questId: String, config: QuestConfig, questData: QuestData): Boolean {
        // Placeholder implementation
        return false
    }

    fun isQuestCompleted(player: Player, questId: String): Boolean {
        val config = QuestConfigHolder.config ?: error("Trying to check completion of quest $questId but QuestConfig is not initialized")
        val questData = getQuestData(player)
        val activeQuests = questData.activeQuests
        if (questId !in activeQuests) return false
        return isVisitQuestCompleted(questId, config, questData) || isKillQuestCompleted(questId, config, questData) || isFetchQuestCompleted(questId, config, questData)
    }


    fun checkAndCompleteQuest(player: Player, questId: String) {
        if (isQuestCompleted(player, questId)) {
            completeQuest(player, questId)
        } else {
            player.error("You haven't completed this quest yet!")
        }
    }

    fun playerHasUnlockedQuest(player: Player, questId: String): Boolean {
        val questData = getQuestData(player)
        return questId in questData.activeQuests
    }

    fun playerHasCompletedQuest(player: Player, questId: String): Boolean {
        val questData = getQuestData(player)
        return questId in questData.completedQuests
    }

    fun resetQuests(player: Player) {
        player.toGeary().setPersisting(QuestData())
    }
}