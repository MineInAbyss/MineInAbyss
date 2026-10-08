package com.mineinabyss.components.lootcrates

import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.DurationSerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
@SerialName("mineinabyss:loot_crate")
class LootCrateSettings(
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val furniture: PrefabKey = LootCrateConstants.DEFAULT_FURNITURE,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val restock: @Serializable(DurationSerializer::class) Duration? = null,
) {
    val furnitureId: String get() = furniture.nexoId

    companion object {
        val DEFAULT = LootCrateSettings()
    }
}

/** The item ID geary registers this prefab under in Nexo */
val PrefabKey.nexoId: String get() = "${namespace}_$key"
