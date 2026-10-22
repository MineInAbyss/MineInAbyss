package com.mineinabyss.features.triggerpoints

import com.mineinabyss.features.dialogs.actions.brokenDialogs
import com.mineinabyss.features.helpers.actionsOf
import com.mineinabyss.features.helpers.loading.KeyedYamlLoader
import com.mineinabyss.features.helpers.loading.gearyYaml
import com.mineinabyss.features.locations.LocationPoint
import com.mineinabyss.features.locations.WarpToAction
import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.Location
import java.nio.file.Path
import java.util.*

// Guards against a typo like 0..2000 filling a huge area
private const val MAX_TRIGGER_BLOCKS = 36
private const val MAX_BOX_VOLUME = 64 * 64 * 64

/** Files may `inherit` others and declare `variables` like dialogs do */
class TriggerPointRegistry(private val root: Path) {
    var points: Map<PrefabKey, TriggerPoint> = emptyMap()
        private set
    var blocks: Map<PrefabKey, List<Location>> = emptyMap()
        private set
    var boxes: Map<UUID, List<ResolvedBox>> = emptyMap()
        private set

    operator fun get(key: PrefabKey) = points[key]

    fun reload(): List<String> {
        val yaml = gearyYaml()
        val files = KeyedYamlLoader.load(root)
        val decoded = files.decodeInherited("trigger point", files.resolveInheritance("trigger point")) {
            yaml.decodeFromYamlNode(TriggerPoint.serializer(), it)
        }

        val blocks = mutableMapOf<PrefabKey, List<Location>>()
        val boxes = mutableListOf<ResolvedBox>()
        val valid = mutableMapOf<PrefabKey, TriggerPoint>()
        decoded.forEach { (key, point) ->
            files.errors += actionProblems(key, point)
            runCatching {
                when (point) {
                    is TriggerBlock -> blocks[key] = resolveBlocks(point)
                    is TriggerBox -> boxes += resolveBox(key, point)
                }
                valid[key] = point
            }.onFailure { files.errors += "Trigger point $key (${files.where(key)}): ${it.message}" }
        }
        points = valid
        this.blocks = blocks
        this.boxes = boxes.groupBy { it.world.uid }

        return files.logErrors(points.size, "trigger points")
    }

    private fun resolveBlocks(point: TriggerBlock): List<Location> {
        val origin = point.location.resolveValid()
        if (point.blockCount > MAX_TRIGGER_BLOCKS) error("it covers ${point.blockCount} trigger blocks, the limit is $MAX_TRIGGER_BLOCKS")
        return point.blocks(origin)
    }

    private fun resolveBox(key: PrefabKey, point: TriggerBox): ResolvedBox {
        val (a, b) = point.pointA.resolveValid() to point.pointB.resolveValid()
        if (a.world != b.world) error("pointA is in ${a.world.name} and pointB in ${b.world.name}, both must be in the same world")
        val box = ResolvedBox.between(key, a, b)
        if (box.bounds.volume > MAX_BOX_VOLUME) error("it covers ${box.bounds.volume.toLong()} blocks, the limit is $MAX_BOX_VOLUME")
        return box
    }

    private fun LocationPoint.resolveValid(): Location = problem()?.let { error(it) } ?: resolve()

    private fun actionProblems(key: PrefabKey, point: TriggerPoint): List<String> {
        val dialogs = point.actions.brokenDialogs().map { "Trigger point $key opens a broken dialog: $it" }
        val destinations = point.actions.actionsOf<WarpToAction>().mapNotNull { action ->
            action.warp.to.problem()?.let { "Trigger point $key has a broken warp_to: $it" }
        }
        return dialogs + destinations
    }
}
