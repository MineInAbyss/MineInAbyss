package com.mineinabyss.features.dialogs.runtime

import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.components.unlocks.PlayerUnlocks
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.getOrSetPersisting
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

val Player.unlocks: Set<String> get() = toGeary().get<PlayerUnlocks>()?.keys.orEmpty()

fun Player.unlock(keys: Collection<String>) {
    if (keys.isEmpty()) return
    toGeary().getOrSetPersisting<PlayerUnlocks> { PlayerUnlocks() }.keys += keys
}

fun Player.unlock(key: String) = unlock(listOf(key))

fun Player.lock(key: String) {
    toGeary().get<PlayerUnlocks>()?.keys?.remove(key)
}

fun Player.clearUnlocks() {
    toGeary().get<PlayerUnlocks>()?.keys?.clear()
}

/** Moves gondola tickets bought before gondolas became warps over to [PlayerUnlocks] */
object GondolaUnlockMigration : Listener {
    private val oldGondolaUnlocks = mapOf(
        "l1_wwgondola_a" to "l1_wwgondola",
        "l1_wwgondola_b" to "l1_wwgondola",
        "l1_grandpier_a" to "l1_grandpier",
        "l1_grandpier_b" to "l1_grandpier",
        "l1_grandpier_c" to "l1_grandpier",
    )

    @EventHandler
    fun PlayerJoinEvent.migrateGondolaUnlocks() {
        val gearyPlayer = player.toGeary()
        val old = gearyPlayer.get<UnlockedGondolas>() ?: return
        player.unlock(old.keys.mapNotNull { oldGondolaUnlocks[it] })
        gearyPlayer.remove<UnlockedGondolas>()
    }
}
