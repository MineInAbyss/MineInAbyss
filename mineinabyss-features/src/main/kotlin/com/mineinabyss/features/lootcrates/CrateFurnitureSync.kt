package com.mineinabyss.features.lootcrates

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.mineinabyss.components.lootcrates.ContainsLoot
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.withGeary
import com.nexomc.nexo.api.NexoFurniture
import com.nexomc.nexo.api.NexoItems
import com.nexomc.nexo.mechanics.MechanicsManager
import com.nexomc.nexo.mechanics.furniture.FurnitureFactory
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic
import org.bukkit.Bukkit
import org.bukkit.entity.ItemDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataType

/**
 * Swaps placed crates to the furniture their loot table uses now.
 * The entity is kept, so looted state, keyed by its UUID, carries over
 */
class CrateFurnitureSync(private val lootCrates: LootCrates) : Listener {
    // Before prefabs load every table falls back to the default furniture, which would swap crates that are correct
    @Volatile
    private var tablesLoaded = false

    // Nexo sends a loaded furniture two ticks later from whatever id it holds then, the same window its own converter uses
    @EventHandler
    fun EntityAddToWorldEvent.retargetLoadedCrate() {
        if (tablesLoaded) (entity as? ItemDisplay)?.retarget()
    }

    /** Swaps every loaded crate, for when tables change while crates are already sent to players */
    fun syncLoaded() {
        tablesLoaded = true
        val packetManager = MechanicsManager.factory<FurnitureFactory>()?.packetManager()
        Bukkit.getWorlds().flatMap { it.getEntitiesByClass(ItemDisplay::class.java) }.forEach { crate ->
            val previous = NexoFurniture.furnitureMechanic(crate) ?: return@forEach
            if (!crate.retarget()) return@forEach
            packetManager?.apply {
                removeHitboxEntityPacket(crate, previous)
                removeBarrierHitboxPacket(crate, previous)
                removeLightMechanicPacket(crate, previous)
                removeTextEntityMechanicPacket(crate, previous)
            }
            NexoFurniture.updateFurniture(crate)
        }
    }

    /** @return whether this crate was pointed at a different furniture */
    private fun ItemDisplay.retarget(): Boolean {
        val loot = withGeary { persistentDataContainer.decode<ContainsLoot>() } ?: return false
        if (lootCrates.table(loot.table) == null) return false

        val current = persistentDataContainer.get(FurnitureMechanic.FURNITURE_KEY, PersistentDataType.STRING)
            ?: return false
        val target = lootCrates.settings(loot.table).furnitureId
        if (current == target || NexoFurniture.furnitureMechanic(target) == null) return false

        persistentDataContainer.set(FurnitureMechanic.FURNITURE_KEY, PersistentDataType.STRING, target)
        NexoItems.itemFromId(target)?.let { customName(it.itemName ?: it.customName) }
        return true
    }
}
