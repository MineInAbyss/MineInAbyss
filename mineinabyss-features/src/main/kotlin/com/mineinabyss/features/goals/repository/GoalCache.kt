package com.mineinabyss.features.goals.repository

import com.mineinabyss.features.goals.dataStore.GoalProgress
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class GoalCache {
    private val players = ConcurrentHashMap<UUID, ConcurrentHashMap<String, GoalProgress>>()

    fun load(player: Player, progress: Map<String, GoalProgress>) {
        players[player.uniqueId] = ConcurrentHashMap(progress)
    }

    fun unload(player: Player) {
        players.remove(player.uniqueId)
    }

    fun isLoaded(player: Player): Boolean  { return  players.containsKey(player.uniqueId) }

    fun all(player: Player): Map<String, GoalProgress> { return  players[player.uniqueId] ?: emptyMap() }

    operator fun get(player: Player, goalId: String): GoalProgress? { return players[player.uniqueId]?.get(goalId) }

    fun remove(player: Player, goalId: String) {
        players[player.uniqueId]?.remove(goalId)
    }

    operator fun set(player: Player, goalId: String, progress: GoalProgress) {
        players[player.uniqueId]?.put(goalId, progress)
    }
}
