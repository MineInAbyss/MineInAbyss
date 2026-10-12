package com.mineinabyss.features.dialogs

import com.charleskorn.kaml.*
import com.mineinabyss.features.dialogs.loading.DialogInheritance
import com.mineinabyss.features.dialogs.loading.DialogVariables
import com.mineinabyss.features.dialogs.model.DialogNode
import com.mineinabyss.features.dialogs.model.DialogRef
import com.mineinabyss.features.helpers.loading.KeyedYamlLoader
import com.mineinabyss.geary.prefabs.PrefabKey
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

class DialogRegistry private constructor(
    private val yaml: Yaml,
    /** Dialogs with every copy merged but variables not filled in yet, so runtime copies can still override them */
    private val resolved: Map<PrefabKey, YamlNode>,
    private val dialogs: Map<PrefabKey, DialogNode>,
    val errors: List<String>,
) {
    operator fun get(key: PrefabKey): DialogNode? = dialogs[key]

    val keys get() = dialogs.keys

    fun copyOf(base: PrefabKey, variables: Map<String, YamlNode>): DialogNode {
        val path = YamlPath.root
        val entries = mapOf(
            YamlScalar(DialogInheritance.INHERIT, path) to YamlScalar(base.full, path),
            YamlScalar(DialogVariables.VARIABLES, path) to YamlMap(variables.mapKeys { YamlScalar(it.key, path) }, path),
        )
        return decode(yaml, DialogInheritance.resolveLoaded(YamlMap(entries, path), resolved::get))
    }

    private val inlineCache = ConcurrentHashMap<YamlNode, DialogNode>()

    fun inline(node: YamlNode): DialogNode = inlineCache.getOrPut(node) {
        decode(yaml, DialogInheritance.resolveLoaded(node, resolved::get))
    }

    fun danglingReferences(): List<String> = dialogs.flatMap { (key, node) ->
        references(node).filter { it !in dialogs }.map { "$key -> $it" }
    }

    private fun references(node: DialogNode): List<PrefabKey> = node.children().flatMap { ref ->
        when (ref) {
            is DialogRef.Reference -> listOf(ref.key)
            is DialogRef.Inline -> references(ref.node)
        }
    }

    companion object {
        private const val LINE = "line"
        private const val LINES = "lines"

        fun empty(yaml: Yaml) = DialogRegistry(yaml, emptyMap(), emptyMap(), emptyList())

        fun load(root: Path, yaml: Yaml): DialogRegistry {
            val files = KeyedYamlLoader.load(root)
            val resolved = DialogInheritance.resolveAll(files.files.mapValues { it.value.node })
            resolved.errors.forEach { (key, error) -> files.errors += "Could not load dialog $key (${files.where(key)}): $error" }
            val dialogs = files.decodeAll("dialog", resolved.nodes) { decode(yaml, it) }
            return DialogRegistry(yaml, resolved.nodes, dialogs, files.errors)
        }

        private fun decode(yaml: Yaml, node: YamlNode): DialogNode =
            yaml.decodeFromYamlNode(DialogNode.serializer(), normalize(DialogVariables.substitute(node)))

        // Lets single-line pages write `line:` instead of `lines:`
        private fun normalize(node: YamlNode): YamlNode = when (node) {
            is YamlList -> YamlList(node.items.map(::normalize), node.path)
            is YamlTaggedNode -> node.copy(innerNode = normalize(node.innerNode))
            is YamlMap -> {
                val hasLines = node.entries.keys.any { it.content == LINES }
                YamlMap(node.entries.entries.associate { (key, value) ->
                    val renamed = if (key.content == LINE && !hasLines) key.copy(content = LINES) else key
                    renamed to normalize(value)
                }, node.path)
            }

            else -> node
        }
    }
}
