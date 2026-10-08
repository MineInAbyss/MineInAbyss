package com.mineinabyss.features.lootcrates

import com.mineinabyss.features.abyss
import com.mineinabyss.features.lootcrates.database.CrateLoot
import com.mineinabyss.features.lootcrates.database.LootCratesDataStore
import com.mineinabyss.idofront.datastore.launchWrite
import com.mineinabyss.idofront.datastore.write
import kotlinx.coroutines.runBlocking
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.uuid.toKotlinUuid

/**
 * Each online player's loot per crate, mirrored from [LootCratesDataStore].
 * Furniture overrides ask this once per viewer on every send, so it must never touch the database
 */
class LootedCache {
    private val loot = ConcurrentHashMap<UUID, MutableMap<UUID, CrateLoot>>()

    fun load(player: Player) {
        val rows = runCatching { runBlocking { player.write { LootCratesDataStore.lootedBy(player.uniqueId.toKotlinUuid()) } } }
            .onFailure { abyss.logger.e("Could not load looted crates for ${player.name}: ${it.message}") }
            .getOrNull() ?: return
        loot[player.uniqueId] = ConcurrentHashMap(rows)
    }

    fun isLoaded(player: Player): Boolean = loot.containsKey(player.uniqueId)

    fun unload(player: Player) {
        loot.remove(player.uniqueId)
    }

    operator fun get(player: Player, crate: UUID): CrateLoot? = loot[player.uniqueId]?.get(crate)

    fun all(player: Player): Map<UUID, CrateLoot> = loot[player.uniqueId].orEmpty()

    fun save(player: Player, crate: UUID, state: CrateLoot) {
        loot[player.uniqueId]?.put(crate, state)
        player.launchWrite { LootCratesDataStore[player, crate.toString()] = state }
    }

    fun forget(player: UUID, crate: UUID) {
        loot[player]?.remove(crate)
    }

    fun forgetAll(crate: UUID) {
        loot.values.forEach { it.remove(crate) }
    }

    /** [player]'s loot for [crate], unless it has restocked since it was rolled */
    fun current(player: Player, crate: UUID, restock: Duration?): CrateLoot? =
        get(player, crate)?.takeUnless { restock != null && Clock.System.now() - it.rolledAt >= restock }

    fun isLooted(player: Player, crate: UUID, restock: Duration?): Boolean =
        current(player, crate, restock)?.emptied == true
}
