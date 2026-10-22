package com.mineinabyss.features.locations

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlNull
import com.charleskorn.kaml.YamlScalar
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.LocationAltSerializer
import org.bukkit.Location
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText

class LocationPoints(private val file: Path) {
    private var points: Map<PrefabKey, Location> = emptyMap()

    val loaded = CompletableFuture<Unit>()

    /** A copy, so callers may change it */
    operator fun get(key: PrefabKey): Location? = points[key]?.clone()

    val keys: Set<PrefabKey> get() = points.keys

    operator fun contains(key: PrefabKey) = key in points

    /** A file that cannot be read as a whole keeps the points loaded before, a broken entry is skipped */
    fun reload(): List<String> = try {
        val errors = mutableListOf<String>()
        read(errors)?.let { points = it }
        errors.forEach { abyss.logger.e(it) }
        abyss.logger.i("Loaded ${points.size} location points")
        errors
    } finally {
        loaded.complete(Unit)
    }

    private fun read(errors: MutableList<String>): Map<PrefabKey, Location>? {
        if (!file.isRegularFile()) return emptyMap()
        val root = try {
            Yaml.default.parseToYamlNode(file.readText())
        } catch (e: Exception) {
            errors += "Could not parse ${file.fileName}, keeping the location points loaded before: ${e.message}"
            return null
        }
        val entries = when (val node = (root as? YamlMap)?.get<YamlNode>(ROOT_KEY)) {
            null, is YamlNull -> return emptyMap()
            is YamlMap -> node.entries
            else -> return null.also { errors += "$ROOT_KEY in ${file.fileName} must be a map of key: location, keeping the location points loaded before" }
        }

        return entries.entries.mapNotNull { (name, value) ->
            val key = PrefabKey.ofOrNull(name.content) ?: return@mapNotNull null.also { errors += "Invalid location point key '${name.content}'" }
            runCatching {
                (value as? YamlScalar)?.let { LocationPointSerializer.checkLiteral(it.content) }
                key to Yaml.default.decodeFromYamlNode(LocationAltSerializer, value)
            }.onFailure { errors += "Could not load location point $key: ${it.message}" }.getOrNull()
        }.toMap()
    }

    companion object {
        private const val ROOT_KEY = "locationPoints"
    }
}
