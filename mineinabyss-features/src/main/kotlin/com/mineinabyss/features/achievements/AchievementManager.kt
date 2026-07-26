package com.mineinabyss.features.achievements

import com.mineinabyss.features.goals.Goal
import com.mineinabyss.features.goals.repository.GoalRepository
import com.mineinabyss.geary.papermc.spawning.locations.RegionService
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.Services
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

class AchievementManager(
    private val config: AchievementsConfig,
    val repository: GoalRepository,
) {
    fun autoStart(player: Player) {
        val completed = repository.allProgress(player).filterValues { it.completed }.keys
        var startedAny = false
        config.achievements.forEach { achievement ->
            if (!achievement.isUnlockedBy(completed)) return@forEach
            if (repository.startGoal(player, achievement.toGoal())) {
                startedAny = true
            }
        }
        if (startedAny)  {
            seedCurrentRegions(player)
        }
    }

    fun onComplete(player: Player, goal: Goal) {
        player.success("Achievement unlocked: ${goal.name}")
        val completed = repository.allProgress(player).filterValues { it.completed }.keys
        var startedAny = false
        config.achievements
            .filter { goal.id in it.requires && it.isUnlockedBy(completed) }
            .forEach { gated ->
                if (repository.startGoal(player, gated.toGoal())) startedAny = true
            }
        if (startedAny) seedCurrentRegions(player)
        // miav is an arbitrary name I picked for the achievement namespace (short for mineInAchievement), less annoying to write than "mineinabyss:" everytime
        val key = NamespacedKey("miav", goal.id)
        val achievement = Bukkit.getAdvancement(key) ?: run { player.error("Couldn't get achievement with key: $key"); return@onComplete }
        // criterion name is the advancement json's last path segment ("mia/id" -> "id"),
        // not the full goal id, since datapack folders can nest deeper than the criterion name does
        val criterion = goal.id.substringAfterLast("/")
        val awarded = player.getAdvancementProgress(achievement).awardCriteria(criterion)
        if (!awarded)
            player.error("Couldn't award criterion '$criterion' for advancement $key, check it matches the json, if you see this as a player, go open a bug report")
    }

    fun onReset(player: Player, goal: Goal) {
        val key = NamespacedKey("miav", goal.id)
        val achievement = Bukkit.getAdvancement(key) ?: run { player.error("Couldn't get achievement with key: $key"); return }
        val criterion = goal.id.substringAfterLast("/")
        player.getAdvancementProgress(achievement).revokeCriteria(criterion)
    }

    private fun seedCurrentRegions(player: Player) {
        val regions = Services.getOrNull<RegionService>()?.regionsAt(player.location) ?: return
        repository.seedRegions(player, regions)
    }
}
