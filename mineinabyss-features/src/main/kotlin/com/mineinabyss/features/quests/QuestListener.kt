package com.mineinabyss.features.quests

import com.mineinabyss.features.quests.QuestManager.getVisitQuestProgress
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class QuestListener(
    val questConfig: QuestConfig,
): Listener {

    @EventHandler
    fun PlayerMoveEvent.onLocationEnter() {
        if (!this.hasExplicitlyChangedBlock()) {
            return
        }
       val activeQuests = QuestManager.getActiveQuests(player)
        val playerVisitedLocations = QuestManager.getVisitedLocations(player)

//        activeQuests.activeVisitQuests.forEach { visitQuest ->
//            visitQuest.locations.forEach { location ->
//                if (!location.visited && location.isInside(to)) {
//                    location.visited = true
//                }
//            }
//        }
        questConfig.visitQuests.forEach { quest ->
            if (!activeQuests.contains(quest.key)) return@forEach
            // there are probably some spacial partitioning tricks to do here at some point
            // but for now we have at most 40 players with a quest containing at most 15 locations
            // so it's only ever 600 checks per move event which is at most 3600 integer comparisons if all 40 players manage to move at 1 block per tick
            // tldr: i'll optimize it later
            quest.value.locations.forEach { location ->
                if (location.name !in playerVisitedLocations && location.isInside(to)) {
                    QuestManager.addVisitedLocation(player, location.name)
                    val progress = getVisitQuestProgress(player, quest.key)
                    player.sendActionBar("${progress.first}/${progress.second} locations visited for current quest!")
                }
            }
        }
    }
}