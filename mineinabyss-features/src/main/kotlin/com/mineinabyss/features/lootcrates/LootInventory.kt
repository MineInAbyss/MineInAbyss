package com.mineinabyss.features.lootcrates

import com.mineinabyss.components.lootcrates.CrateSounds
import com.mineinabyss.geary.prefabs.PrefabKey
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import java.util.UUID
import kotlin.time.Instant

/** A player's own loot from [crate], [looting] is null for previews that are not saved */
class LootInventory(
    val crate: ItemDisplay?,
    val sounds: CrateSounds,
    title: Component,
    val looting: Looting?,
) : InventoryHolder {
    class Looting(val crateId: UUID, val table: PrefabKey, val rolledAt: Instant)

    private val inventory = Bukkit.createInventory(this, 27, title)

    override fun getInventory(): Inventory = inventory
}
