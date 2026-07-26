package com.mineinabyss.features.lootcrates.database

import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.datastore.KeyedMinecraftDataStore
import com.mineinabyss.idofront.serialization.InstantAsEpochSecondsSerializer
import com.mineinabyss.idofront.serialization.LocationSerializer
import kotlinx.serialization.Serializable
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.sqlite.datastore.keyedJsonTable
import org.bukkit.Location
import kotlin.time.Instant

@Serializable
data class LootCrate(
    val dateLooted: @Serializable(with = InstantAsEpochSecondsSerializer::class) Instant,
    val lootType: PrefabKey,
)

object LootCratesDataStore : KeyedMinecraftDataStore<Location, LootCrate>(keyedJsonTable("loot_crates") {
    index("location", "key")
}, LocationSerializer, LootCrate.serializer()) {
    context(tx: WriteTransaction)
    fun deleteAtLocation(location: Location) {
        val encoded = encodeKey(location)
        tx.exec("DELETE FROM $table WHERE key = jsonb(?)", encoded)
    }
}
