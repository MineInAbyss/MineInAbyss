package com.mineinabyss.features.cinematics

import com.mineinabyss.features.helpers.loading.KeyedYamlLoader
import com.mineinabyss.geary.prefabs.PrefabKey
import java.nio.file.Path

internal class CinematicRegistry(private val root: Path) {
    var scenes: Map<PrefabKey, CinematicScene> = emptyMap()
        private set

    operator fun get(key: PrefabKey) = scenes[key]

    fun reload(): List<String> {
        val files = KeyedYamlLoader.load(root)
        scenes = files.decodeAll("cinematic", CinematicScene.serializer())
        return files.logErrors(scenes.size, "cinematics")
    }
}
