package com.mineinabyss.features.warps

import com.mineinabyss.features.helpers.loading.KeyedYamlLoader
import com.mineinabyss.geary.prefabs.PrefabKey
import java.nio.file.Path

class WarpRegistry(private val root: Path, private val config: WarpsConfig) {
    var warps: Map<PrefabKey, WarpDefinition> = emptyMap()
        private set

    operator fun get(key: PrefabKey) = warps[key]

    fun reload(): List<String> {
        val files = KeyedYamlLoader.load(root)
        warps = files.decodeAll("warp", WarpDefinition.serializer())
        warps.forEach { (key, warp) -> files.errors += problems(key, warp) }

        return files.logErrors(warps.size, "warps")
    }

    private fun problems(key: PrefabKey, warp: WarpDefinition): List<String> = buildList {
        if (warp.stops.size < 2) add("Warp $key needs at least two stops")
        warp.routes.flatMap { listOfNotNull(it.from, it.to) }.distinct().filter { it !in warp.stops }
            .forEach { add("Warp $key has a route through unknown stop $it") }
        (warp.transition as? TransitionRef.Named)?.name?.takeIf { it !in config.transitions }
            ?.let { add("Warp $key uses unknown transition $it") }
    }
}
