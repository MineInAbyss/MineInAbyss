package com.mineinabyss.features.triggerpoints

import com.mineinabyss.features.helpers.OffsetArea
import com.mineinabyss.features.helpers.OffsetAreasSerializer
import com.mineinabyss.features.locations.LocationPoint
import com.mineinabyss.geary.actions.Tasks
import com.mineinabyss.geary.autoscan.ExcludeAutoScan
import com.mineinabyss.geary.prefabs.PrefabKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.BoundingBox

const val TRIGGER_POINT_KEY = "trigger_point"

/** Runs [actions] when a player enters it */
@Serializable
@ExcludeAutoScan
sealed interface TriggerPoint {
    val actions: Tasks
}

/** Nexo trigger blocks on [location] and the blocks [offset] covers around it */
@Serializable
@ExcludeAutoScan
@SerialName("BLOCK")
class TriggerBlock(
    val location: LocationPoint,
    val offset: @Serializable(OffsetAreasSerializer::class) List<OffsetArea> = emptyList(),
    override val actions: Tasks,
) : TriggerPoint {
    val blockCount get() = offset.sumOf { it.blockCount } + 1

    fun blocks(origin: Location): List<Location> {
        val block = origin.toBlockLocation()
        val around = offset.flatMap { area -> area.blockOffsets().map { (x, y, z) -> block.clone().add(x.toDouble(), y.toDouble(), z.toDouble()) } }
        return (listOf(block) + around).distinct()
    }
}

/** Every block between [pointA] and [pointB], both included */
@Serializable
@ExcludeAutoScan
@SerialName("BOX")
class TriggerBox(
    val pointA: LocationPoint,
    val pointB: LocationPoint,
    override val actions: Tasks,
) : TriggerPoint

class ResolvedBox(val key: PrefabKey, val world: World, val bounds: BoundingBox) {
    operator fun contains(location: Location) = location.world == world && bounds.contains(location.x, location.y, location.z)

    companion object {
        fun between(key: PrefabKey, a: Location, b: Location): ResolvedBox {
            val min = Location(a.world, minOf(a.blockX, b.blockX).toDouble(), minOf(a.blockY, b.blockY).toDouble(), minOf(a.blockZ, b.blockZ).toDouble())
            val max = Location(a.world, maxOf(a.blockX, b.blockX) + 1.0, maxOf(a.blockY, b.blockY) + 1.0, maxOf(a.blockZ, b.blockZ) + 1.0)
            return ResolvedBox(key, a.world, BoundingBox.of(min, max))
        }
    }
}
