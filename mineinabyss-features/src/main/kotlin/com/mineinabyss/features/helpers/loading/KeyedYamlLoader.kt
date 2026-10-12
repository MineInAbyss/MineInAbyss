package com.mineinabyss.features.helpers.loading

import com.charleskorn.kaml.AnchorsAndAliases
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlNode
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.prefabs.PrefabKey
import kotlinx.serialization.KSerializer
import java.nio.file.Path
import kotlin.io.path.*

data class KeyedYamlFile(val path: Path, val node: YamlNode)

class KeyedYamlFiles(private val root: Path, val files: Map<PrefabKey, KeyedYamlFile>, errors: List<String>) {
    val errors: MutableList<String> = errors.toMutableList()

    fun where(key: PrefabKey): Path? = files[key]?.let { root.relativize(it.path) }

    fun <T> decodeAll(kind: String, serializer: KSerializer<T>, yaml: Yaml = gearyYaml()): Map<PrefabKey, T> =
        decodeAll(kind) { yaml.decodeFromYamlNode(serializer, it) }

    fun <T> decodeAll(
        kind: String,
        nodes: Map<PrefabKey, YamlNode> = files.mapValues { it.value.node },
        decode: (YamlNode) -> T,
    ): Map<PrefabKey, T> = nodes.mapNotNull { (key, node) ->
        runCatching { decode(node) }
            .onFailure { errors += "Could not load $kind $key (${where(key)}): ${it.message}" }
            .getOrNull()?.let { key to it }
    }.toMap()

    fun logErrors(count: Int, kindPlural: String): List<String> {
        errors.forEach { abyss.logger.e(it) }
        abyss.logger.i("Loaded $count $kindPlural")
        return errors
    }
}

/**
 * Reads `root/<namespace>/<any/path>/<key>.yml` files into raw yaml nodes keyed like Geary prefabs.
 * Files directly inside [root] are not keyed and are skipped, so feature configs can live next to them
 */
object KeyedYamlLoader {
    private val parser = Yaml(configuration = YamlConfiguration(anchorsAndAliases = AnchorsAndAliases.Permitted()))

    @OptIn(ExperimentalPathApi::class)
    fun load(root: Path, excludedNamespaces: Set<String> = emptySet()): KeyedYamlFiles {
        val files = mutableMapOf<PrefabKey, KeyedYamlFile>()
        val errors = mutableListOf<String>()
        if (!root.isDirectory()) return KeyedYamlFiles(root, files, errors)

        root.listDirectoryEntries().filter { it.isDirectory() && it.name !in excludedNamespaces }.sorted().forEach { namespaceDir ->
            namespaceDir.walk().filter { it.isRegularFile() && it.extension in setOf("yml", "yaml") }.sorted().forEach { path ->
                val key = PrefabKey.ofOrNull("${namespaceDir.name}:${path.nameWithoutExtension}") ?: return@forEach run {
                    errors += "Invalid key for ${root.relativize(path)}, names cannot contain ':'"
                }
                val existing = files[key]
                if (existing != null) {
                    errors += "Duplicate key $key in ${root.relativize(path)} and ${root.relativize(existing.path)}"
                    return@forEach
                }
                runCatching { parser.parseToYamlNode(path.readText()) }
                    .onSuccess { files[key] = KeyedYamlFile(path, it) }
                    .onFailure { errors += "Could not parse ${root.relativize(path)}: ${it.message}" }
            }
        }
        return KeyedYamlFiles(root, files, errors)
    }
}
