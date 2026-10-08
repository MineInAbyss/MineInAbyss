package com.mineinabyss.features.lootcrates

import com.mineinabyss.components.lootcrates.ContainsLoot
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.datastore.remove
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.geary.serialization.setPersisting
import com.nexomc.nexo.api.NexoFurniture
import com.nexomc.nexo.mechanics.MechanicsManager
import com.nexomc.nexo.mechanics.furniture.FurnitureFactory
import net.kyori.adventure.sound.Sound
import org.bukkit.NamespacedKey
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

private val CUSTOM_LOOT_KEY = NamespacedKey("mineinabyss", "custom_loot")

val ItemDisplay.containsLoot: ContainsLoot?
    get() = withGeary { toGearyOrNull()?.get<ContainsLoot>() }

fun ItemDisplay.setContainsLoot(loot: ContainsLoot?) = withGeary {
    val gearyEntity = toGearyOrNull() ?: return@withGeary
    if (loot == null) {
        gearyEntity.remove<ContainsLoot>()
        persistentDataContainer.remove<ContainsLoot>()
    } else gearyEntity.setPersisting(loot)
    gearyEntity.encodeComponentsTo(this@setContainsLoot)
}

/**
 * Geary drops components it cannot decode yet and rewrites the entity without them.
 * Plugins like TrainCarts load chunks before this plugin registers its components, so crates there lose theirs on every boot
 */
fun ItemDisplay.restoreContainsLoot() {
    if (containsLoot != null) return
    withGeary { persistentDataContainer.decode<ContainsLoot>() }?.let(::setContainsLoot)
}

val ItemStack.containsLoot: ContainsLoot?
    get() = with(gearyPaper.worldManager.global) { persistentDataContainer.decode<ContainsLoot>() }

fun ItemDisplay.playCrateSound(sound: Sound) =
    location.toCenterLocation().let { world.playSound(sound, it.x, it.y, it.z) }

/** Resends this crate to [player] alone, [LootCrateItemOverride] decides what they see */
fun ItemDisplay.resendTo(player: Player) {
    if (!isValid || player !in trackedBy) return

    val mechanic = NexoFurniture.furnitureMechanic(this) ?: return
    MechanicsManager.factory<FurnitureFactory>()?.packetManager()?.sendFurnitureMetadataPacket(this, mechanic, player)
}

/** Items staff put in a crate using the custom loot table, handed out as is to everyone */
var ItemDisplay.customLoot: List<ItemStack?>
    get() = persistentDataContainer.get(CUSTOM_LOOT_KEY, PersistentDataType.BYTE_ARRAY)
        ?.let(ItemStack::deserializeItemsFromBytes)
        ?.map { it.takeUnless(ItemStack::isEmpty) }
        .orEmpty()
    set(items) {
        if (items.all { it == null || it.isEmpty }) persistentDataContainer.remove(CUSTOM_LOOT_KEY)
        else persistentDataContainer.set(
            CUSTOM_LOOT_KEY,
            PersistentDataType.BYTE_ARRAY,
            ItemStack.serializeItemsAsBytes(items.map { it ?: ItemStack.empty() })
        )
    }
