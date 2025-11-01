package com.mineinabyss.features.quests

import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

//fun unlockQuest(player: Player, questId: String) {
//    val config = QuestConfigHolder.config ?: error("Trying to unlock quest $questId but QuestConfig is not initialized")
//    val playerActiveQuests  = player.toGearyOrNull()?.get<PlayerActiveQuests>() ?: error("Could not unlock quest $questId: PlayerActiveQuests component not found")
//    if (questId !in config.visitQuests.keys) {
//        error("Trying to unlock quest $questId but it does not exist in the QuestConfig")
//    }
//    if (questId in playerActiveQuests.activeQuests) {
//        error("Trying to unlock quest $questId but player already has it active")
//    }
//    playerActiveQuests.addQuest(player, questId)
//}

//fun completeQuest(player: Player, questId: String) {
//    val config = QuestConfigHolder.config ?: error("Trying to complete quest $questId but QuestConfig is not initialized")
//    player.toGearyOrNull()?.get<PlayerActiveQuests>()?.completeQuest(player, questId)
//    val gearyRewards = config.visitQuests[questId]?.gearyRewards
//    if (gearyRewards != null) {
//        for ((item, amount) in gearyRewards) {
//            val gearyItems = player.world.toGeary().getAddon(ItemTracking)
//            val gearyItem = gearyItems.createItem(item) ?: error("Failed to complete quest $questId: Geary prefab $item not found")
//            gearyItem.amount = amount.coerceIn(1, gearyItem.maxStackSize)
//            player.inventory.addItem(gearyItem)
//        }
//    }
//    val vanillaRewards = config.visitQuests[questId]?.vanillaRewards ?: return
//    for ((itemName, amount) in vanillaRewards) {
//        val material = Material.matchMaterial(itemName) ?: error("Failed to complete quest $questId: Material $itemName not found")
//        val itemStack = ItemStack(material)
//        itemStack.amount = amount.coerceIn(1, itemStack.maxStackSize)
//        player.inventory.addItem(itemStack)
//    }
//}

//fun isVisitQuestCompleted(questId: String, config: QuestConfig, player: Player): Boolean {
//    val visitQuest = config.visitQuests[questId] ?: return
//
//    return visitQuest.locations.all { locationData ->
//        locationData.name in playerActiveQuests.visitedLocations
//    }
//}

//fun isFetchQuestCompleted(questId: String, config: QuestConfig, playerActiveQuests: PlayerActiveQuests): Boolean {
//    // Placeholder implementation
//    return false
//}
//
//fun isKillQuestCompleted(questId: String, config: QuestConfig, playerActiveQuests: PlayerActiveQuests): Boolean {
//    // Placeholder implementation
//    return false
//}

//fun isQuestCompleted(player: Player, questId: String): Boolean {
//    val config = QuestConfigHolder.config ?: error("Trying to check completion of quest $questId but QuestConfig is not initialized")
//    val playerActiveQuests = player.toGearyOrNull()?.get<PlayerActiveQuests>() ?: return false
//    val activeQuests = playerActiveQuests.activeQuests
//    if (questId !in activeQuests) return false
//    return isVisitQuestCompleted(questId, config, playerActiveQuests) || isKillQuestCompleted(questId, config, playerActiveQuests) || isFetchQuestCompleted(questId, config, playerActiveQuests)
//}

//fun checkAndCompleteQuest(player: Player, questId: String) {
//    if (isQuestCompleted(player, questId)) {
//        completeQuest(player, questId)
//    }
//}

//fun getVisitQuestProgress(questId: String, config: QuestConfig, playerActiveQuests: PlayerActiveQuests): Pair<Int, Int> {
//    val visitQuest = config.visitQuests[questId] ?: return 0 to 0
//    val totalLocations = visitQuest.locations.size
//    val visitedLocations = visitQuest.locations.count { locationData ->
//        locationData.name in playerActiveQuests.visitedLocations
//    }
//    return visitedLocations to totalLocations
//}

//fun Player.hasUnlockedQuest(questId: String): Boolean {
//    val playerActiveQuests = this.toGearyOrNull()?.get<PlayerActiveQuests>() ?: return false
//    return questId in playerActiveQuests.activeQuests
//}
//
//fun Player.hasCompletedQuest(questId: String): Boolean {
//    val playerActiveQuests = this.toGearyOrNull()?.get<PlayerActiveQuests>() ?: return false
//    return questId in playerActiveQuests.completedQuests
//}