package com.mineinabyss.features.lootcrates

import com.nexomc.nexo.api.FurnitureItemOverride
import com.nexomc.nexo.items.ItemBuilder
import org.bukkit.inventory.ItemStack

/**
 * Shows the furniture's `looted` state only to players who looted the crate.
 * Nexo states belong to the entity and would show to everyone, so the crate itself never enters it
 */
class LootCrateItemOverride(private val lootCrates: LootCrates) : FurnitureItemOverride {
    override fun itemFor(furniture: FurnitureItemOverride.Furniture): ItemStack? {
        val crate = furniture.baseEntity
        val loot = crate.containsLoot ?: return null
        val restock = lootCrates.settings(loot.table).restock

        if (!lootCrates.cache.isLooted(furniture.player, crate.uniqueId, restock)) return null

        val looted = furniture.mechanic.states?.states?.get(LOOTED_STATE) ?: return null
        return ItemBuilder(furniture.item.clone()).also(looted::stateItem).build()
    }

    companion object {
        const val LOOTED_STATE = "looted"
    }
}
