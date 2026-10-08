package com.mineinabyss.features.lootcrates

import com.mineinabyss.components.lootcrates.ContainsLoot
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.time.ticks
import com.nexomc.nexo.api.NexoFurniture
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.data.Directional
import org.bukkit.entity.ItemDisplay
import java.nio.file.Path
import kotlin.io.path.readLines

/** Replaces old chest loot crates with furniture crates, reading `<world> <x> <y> <z>` lines where `#` starts a comment */
class ChestCrateMigration(private val lootCrates: LootCrates, private val overrides: CrateOverrides) {
    data class Target(val world: String, val x: Int, val y: Int, val z: Int) {
        override fun toString() = "$world $x $y $z"
    }

    class Report(val migrated: List<Target>, val alreadyMigrated: List<Target>, val failed: Map<Target, String>)

    private sealed interface Outcome {
        data object Migrated : Outcome
        data object AlreadyMigrated : Outcome
        data class Failed(val reason: String) : Outcome
    }

    fun parse(file: Path): List<Target> = file.readLines().mapIndexedNotNull { index, line ->
        val parts = line.substringBefore('#').trim().split(WHITESPACE).filter(String::isNotEmpty)
        if (parts.isEmpty()) return@mapIndexedNotNull null

        val coords = parts.drop(1).mapNotNull(String::toIntOrNull)
        require(parts.size == 4 && coords.size == 3) { "Line ${index + 1} is not <world> <x> <y> <z>: $line" }
        Target(parts[0], coords[0], coords[1], coords[2])
    }

    /** Handles one chunk per tick so a long list never loads the whole map at once, must be called from the main thread */
    suspend fun run(targets: List<Target>, dryRun: Boolean): Report {
        val outcomes = buildList<Pair<Target, Outcome>> {
            for (group in targets.groupBy { Triple(it.world, it.x shr 4, it.z shr 4) }.values) {
                delay(1.ticks)
                addAll(migrateChunk(group, dryRun))
            }
        }

        return Report(
            migrated = outcomes.filter { (_, outcome) -> outcome == Outcome.Migrated }.map { it.first },
            alreadyMigrated = outcomes.filter { (_, outcome) -> outcome == Outcome.AlreadyMigrated }.map { it.first },
            failed = outcomes
                .mapNotNull { (target, outcome) -> (outcome as? Outcome.Failed)?.let { target to it.reason } }
                .toMap(),
        )
    }

    private suspend fun migrateChunk(group: List<Target>, dryRun: Boolean): List<Pair<Target, Outcome>> {
        val first = group.first()
        val world = world(first.world) ?: return group.failAll("unknown world")
        val chunk = runCatching { world.getChunkAtAsync(first.x shr 4, first.z shr 4).await() }
            .getOrElse { return group.failAll("chunk failed to load: ${it.message}") }

        chunk.addPluginChunkTicket(abyss)
        try {
            // Entities load separately from blocks, and checking for an existing crate needs them
            var waitedTicks = 0
            while (!chunk.isEntitiesLoaded && waitedTicks++ < MAX_ENTITY_WAIT_TICKS) delay(1.ticks)

            return group.map { target ->
                target to runCatching { migrate(world, target, dryRun) }
                    .getOrElse { Outcome.Failed("${it::class.simpleName}: ${it.message}") }
            }
        } finally {
            chunk.removePluginChunkTicket(abyss)
        }
    }

    private fun List<Target>.failAll(reason: String) = map { it to Outcome.Failed(reason) }

    private fun migrate(world: World, target: Target, dryRun: Boolean): Outcome {
        val block = world.getBlockAt(target.x, target.y, target.z)
        val chest = block.state as? Chest
        val loot = chest?.withGeary { chest.persistentDataContainer.decode<ContainsLoot>() }
        if (chest == null || loot == null) {
            return if (crateAt(block) != null) Outcome.AlreadyMigrated
            else Outcome.Failed("no loot chest here, found ${block.type.key.asMinimalString()}")
        }

        val table = loot.table.withCurrentNamespace()
        if (lootCrates.table(table) == null) return Outcome.Failed("loot table $table does not exist")

        val furnitureId = lootCrates.settings(table).furnitureId
        if (NexoFurniture.furnitureMechanic(furnitureId) == null) return Outcome.Failed("furniture $furnitureId does not exist")

        if (dryRun) return Outcome.Migrated

        val original = block.state
        val facing = (block.blockData as? Directional)?.facing ?: BlockFace.SOUTH
        // Furniture needs the space the chest is in, and clearing first keeps the chest from dropping its items
        chest.blockInventory.clear()
        block.setType(Material.AIR, false)

        val crate = NexoFurniture.place(furnitureId, block.location, facing.yaw(), BlockFace.UP)
            ?: return Outcome.Failed("Nexo could not place $furnitureId").also { original.update(true, false) }

        crate.setContainsLoot(ContainsLoot(table))
        if (crate.containsLoot == null) {
            NexoFurniture.furnitureMechanic(crate)?.removeBaseEntity(crate)
            original.update(true, false)
            return Outcome.Failed("crate entity is not tracked by geary")
        }

        overrides.cover(furnitureId)
        NexoFurniture.refreshFurnitureItem(crate)
        return Outcome.Migrated
    }

    private fun crateAt(block: Block): ItemDisplay? = block.world
        .getNearbyEntitiesByType(ItemDisplay::class.java, block.location.toCenterLocation(), 0.75)
        .firstOrNull { it.containsLoot != null }

    private fun world(name: String): World? =
        Bukkit.getWorld(name) ?: NamespacedKey.fromString(name)?.let(Bukkit::getWorld)

    private fun PrefabKey.withCurrentNamespace(): PrefabKey =
        LEGACY_NAMESPACES[namespace]?.let { PrefabKey.of(it, key) } ?: this

    private fun BlockFace.yaw() = when (this) {
        BlockFace.NORTH -> 180f
        BlockFace.EAST -> -90f
        BlockFace.WEST -> 90f
        else -> 0f
    }

    private companion object {
        // Loot tables moved from their own namespace into mineinabyss, but chests placed before that still name the old one
        val LEGACY_NAMESPACES = mapOf("mia-loot" to "mineinabyss")

        const val MAX_ENTITY_WAIT_TICKS = 100
        val WHITESPACE = Regex("\\s+")
    }
}
