package com.mineinabyss.features.helpers.loading

import com.charleskorn.kaml.AnchorsAndAliases
import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlNode
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.serialization.SerializableComponents
import kotlinx.serialization.KSerializer
import java.nio.file.Path
import kotlin.io.path.*

// Geary actions and conditions look up their serializers through the Geary world in this module
fun gearyYaml() = Yaml(
    serializersModule = gearyPaper.worldManager.global.getAddon(SerializableComponents).formats.module,
    configuration = YamlConfiguration(
        strictMode = false,
        polymorphismStyle = PolymorphismStyle.Property,
        polymorphismPropertyName = "type",
        anchorsAndAliases = AnchorsAndAliases.Permitted(),
    ),
)

internal class KeyedYamlFile(val path: Path, val node: YamlNode)

class KeyedYamlFiles internal constructor(
    private val root: Path,
    private val files: Map<PrefabKey, KeyedYamlFile>,
    val errors: MutableList<String>,
) {
    private val nodes: Map<PrefabKey, YamlNode> get() = files.mapValues { it.value.node }

    fun where(key: PrefabKey): Path? = files[key]?.let { root.relativize(it.path) }

    /** Every file merged with what it inherits, templates included */
    fun resolveInheritance(kind: String): Map<PrefabKey, YamlNode> {
        val resolved = YamlInheritance.resolveAll(nodes)
        resolved.errors.forEach { (key, error) -> errors += "Could not load $kind $key (${where(key)}): $error" }
        return resolved.nodes
    }

    /** Decodes every [resolved] file except templates, with its variables filled in */
    fun <T> decodeInherited(kind: String, resolved: Map<PrefabKey, YamlNode>, decode: (YamlNode) -> T): Map<PrefabKey, T> =
        decodeAll(kind, YamlInheritance.withoutTemplates(resolved)) { decode(YamlVariables.substitute(it)) }

    fun <T> decodeAll(kind: String, serializer: KSerializer<T>): Map<PrefabKey, T> {
        val yaml = gearyYaml()
        return decodeAll(kind) { yaml.decodeFromYamlNode(serializer, it) }
    }

    fun <T> decodeAll(
        kind: String,
        nodes: Map<PrefabKey, YamlNode> = this.nodes,
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

class KeyedRegistry<T>(
    private val root: Path,
    private val kind: String,
    private val kindPlural: String,
    private val serializer: KSerializer<T>,
    private val problem: (T) -> String? = { null },
) {
    var entries: Map<PrefabKey, T> = emptyMap()
        private set

    operator fun get(key: PrefabKey) = entries[key]

    fun reload(): List<String> {
        val files = KeyedYamlLoader.load(root)
        entries = files.decodeAll(kind, serializer)
        entries.forEach { (key, entry) -> problem(entry)?.let { files.errors += "${kind.replaceFirstChar(Char::uppercase)} $key $it" } }
        return files.logErrors(entries.size, kindPlural)
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
