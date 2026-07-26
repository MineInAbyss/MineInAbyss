package com.mineinabyss.features.goals.repository

import com.mineinabyss.features.goals.ConditionProgress
import com.mineinabyss.features.goals.FactKind
import com.mineinabyss.features.goals.Goal
import com.mineinabyss.features.goals.dataStore.GoalProgress
import com.mineinabyss.features.goals.dataStore.GoalProgressStore
import com.mineinabyss.idofront.datastore.launchWrite
import com.mineinabyss.idofront.datastore.read
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import kotlin.time.Clock


class GoalRepository(
    private val goals: List<Goal>,
    private val cache: GoalCache,
    private val store: GoalProgressStore,
) {
    var onComplete: (Player, Goal) -> Unit = onComplete@{ player, goal ->
        player.success("Goal completed: ${goal.name}") //TODO proper completion notification/rewards

    }

    suspend fun loadPlayer(player: Player) {
        val stored = player.read { store.getAll(player) }
        // only store online players
        if (player.isOnline) cache.load(player, stored)
    }

    fun unloadPlayer(player: Player)  { return  cache.unload(player) }

    fun progress(player: Player, goalId: String): GoalProgress? { return  cache[player, goalId] }

    fun allProgress(player: Player): Map<String, GoalProgress>  {return  cache.all(player) }

    fun hasGoal(player: Player, goal: Goal): Boolean {
        return cache[player, goal.id] != null
    }

    fun startGoal(player: Player, goal: Goal): Boolean {
        if (cache[player, goal.id] != null) return false // already tracked
        persist(player, goal.id, GoalProgress())
        return true // tracking start
    }

    fun forceComplete(player: Player, goal: Goal) {
        val current = cache[player, goal.id]
        if (current?.completed == true) return
        val progress = (current ?: GoalProgress())
            .copy(completed = true, completionTime = Clock.System.now())
        persist(player, goal.id, progress)
        onComplete(player, goal)
    }


    // For goals whose rewards are claimed after completion (quests); updateProgress won't touch completed goals
    fun markRewardClaimed(player: Player, goal: Goal) {
        val current = cache[player, goal.id] ?: return
        if (current.rewardClaimed) return
        persist(player, goal.id, current.copy(rewardClaimed = true))
    }

    // reset progress
    fun resetGoal(player: Player, goal: Goal) {
        if (cache[player, goal.id] == null) return
        persist(player, goal.id, GoalProgress())
    }

    fun removeGoal(player: Player, goal: Goal) {
        cache.remove(player, goal.id)
        player.launchWrite { store.remove(player, goal.id) }
    }

    fun recordFact(player: Player, kind: FactKind, value: String = "true", amount: Int = 1) { // true default value for one-off facts
        goals.forEach { goal -> applyFact(player, goal, kind, value, amount) }
    }

    // Seeds regions the player is already standing in, since enter events only fire on transitions
    fun seedRegions(player: Player, regions: Collection<String>) {
        regions.forEach { recordFact(player, FactKind.REGION_ENTER, it) }
    }

    private fun applyFact(player: Player, goal: Goal, kind: FactKind, value: String, amount: Int) {
        if (!goal.tracksAny(kind, value)) return
        updateProgress(player, goal) { progress ->
            val updated = progress.conditions.toMutableMap()
            goal.conditions.forEachIndexed { i, condition ->
                if (condition.tracks(kind, value))
                    updated[i] = condition.advance(updated[i] ?: ConditionProgress(), value, amount)
            }
            progress.copy(conditions = updated)
        }
    }

    fun updateProgress(player: Player, goal: Goal, update: (GoalProgress) -> GoalProgress) {
        val current = cache[player, goal.id] ?: return
        if (current.completed) return
        var updated = update(current)
        if (updated == current) return
        val nowCompleted = goal.validate(updated)
        if (nowCompleted)  {
            updated = updated.copy(completed = true, completionTime = Clock.System.now())
        }
        persist(player, goal.id, updated)
        if (nowCompleted)  {
            onComplete(player, goal)
        }
    }

    // Store progress on db
    private fun persist(player: Player, goalId: String, progress: GoalProgress) {
        cache[player, goalId] = progress
        player.launchWrite { store[player, goalId] = progress }
    }
}
