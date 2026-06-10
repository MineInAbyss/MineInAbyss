package com.mineinabyss.features.achievements.data

import com.mineinabyss.idofront.datastore.launchWrite
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent

/**
 * Syncs Minecraft advancements -> Our data store
 */
class AdvancementSyncListener : Listener {
    @EventHandler
    fun PlayerAdvancementDoneEvent.onAdvancementDone() {
        player.launchWrite {
            AchievementStore[player, advancement.key.asString()] = AchievementProgress(completed = true)
        }
    }
}