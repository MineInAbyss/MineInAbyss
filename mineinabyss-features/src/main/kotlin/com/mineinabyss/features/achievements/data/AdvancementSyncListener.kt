package com.mineinabyss.features.achievements.data

import com.mineinabyss.features.goals.dataStore.GoalProgress
import com.mineinabyss.idofront.datastore.launchWrite
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import kotlin.time.Clock

/**
 * Syncs Minecraft advancements -> Our data store
 */
class AdvancementSyncListener : Listener {
    @EventHandler
    fun PlayerAdvancementDoneEvent.onAdvancementDone() {
        player.launchWrite {
            AdvancementStore[player, advancement.key.asString()] =
                GoalProgress(completed = true, completionTime = Clock.System.now())
        }
    }
}
