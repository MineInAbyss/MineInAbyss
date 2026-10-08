package com.mineinabyss.features.lootcrates.database

import com.mineinabyss.features.abyss
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.datastore.KeyedMinecraftDataStore
import com.mineinabyss.idofront.serialization.InstantAsEpochSecondsSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.sqlite.datastore.keyedJsonTable
import org.bukkit.inventory.ItemStack
import java.util.Base64
import java.util.UUID
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class CrateLoot(
    /** When this player's loot was rolled, restocking counts from here */
    val rolledAt: @Serializable(with = InstantAsEpochSecondsSerializer::class) Instant,
    val table: PrefabKey,
    /** What the player left behind, see [encodeLeftovers], null once they emptied it */
    val leftovers: String? = null,
) {
    val emptied: Boolean get() = leftovers == null
}

fun List<ItemStack?>.encodeLeftovers(): String? {
    if (all { it == null || it.isEmpty }) return null

    val bytes = ItemStack.serializeItemsAsBytes(map { it ?: ItemStack.empty() })
    return Base64.getEncoder().encodeToString(bytes)
}

fun String.decodeLeftovers(): List<ItemStack?> =
    ItemStack.deserializeItemsFromBytes(Base64.getDecoder().decode(this))
        .map { it.takeUnless(ItemStack::isEmpty) }

/** Keyed by the crate furniture's entity UUID, so a crate placed where another stood starts fresh */
object LootCratesDataStore : KeyedMinecraftDataStore<String, CrateLoot>(
    keyedJsonTable("crate_loot") { index("crate", "key") },
    String.serializer(),
    CrateLoot.serializer(),
) {
    context(tx: WriteTransaction)
    fun deleteCrate(crate: UUID) {
        tx.exec("DELETE FROM $table WHERE key = jsonb(?)", encodeKey(crate.toString()))
    }

    context(tx: WriteTransaction)
    fun delete(player: Uuid, crate: UUID) {
        tx.exec("DELETE FROM $table WHERE id = ? AND key = jsonb(?)", player, encodeKey(crate.toString()))
    }

    context(tx: Transaction)
    fun countLooted(crate: UUID): Long =
        tx.getSingle("SELECT count(*) FROM $table WHERE key = jsonb(?)", encodeKey(crate.toString())) { getLong(0) }

    context(tx: Transaction)
    fun lootedBy(player: Uuid): Map<UUID, CrateLoot> =
        tx.getList("SELECT json(key), json(data) FROM $table WHERE id = ? AND data IS NOT NULL", player) {
            val key = getText(0)
            runCatching {
                UUID.fromString(json.decodeFromString(keySerializer, key)) to json.decodeFromString(serializer, getText(1))
            }.onFailure { abyss.logger.w("Skipping unreadable looted crate $key for $player: ${it.message}") }.getOrNull()
        }.filterNotNull().toMap()
}
