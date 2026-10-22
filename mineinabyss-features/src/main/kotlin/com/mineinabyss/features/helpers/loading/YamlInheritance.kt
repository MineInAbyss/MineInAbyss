package com.mineinabyss.features.helpers.loading

import com.charleskorn.kaml.*
import com.mineinabyss.geary.prefabs.PrefabKey

class YamlLoadException(message: String) : Exception(message)

private class MissingBase(val key: PrefabKey) : Exception()

class ResolvedYaml(val nodes: Map<PrefabKey, YamlNode>, val errors: Map<PrefabKey, String>)

/**
 * A map with `inherit: ns:key` is a copy of that file with the map's other keys deep-merged over it.
 * Maps merge per key, lists and scalars replace.
 * `inherit` must be a literal key, variables are only filled in after every copy is resolved.
 * A file with `template: true` is only a base for copies and is not loaded itself, copies do not inherit the flag
 */
object YamlInheritance {
    const val INHERIT = "inherit"
    private const val TEMPLATE = "template"

    fun withoutTemplates(nodes: Map<PrefabKey, YamlNode>) =
        nodes.filterValues { (it as? YamlMap)?.getScalar(TEMPLATE)?.toBoolean() != true }

    fun resolveAll(files: Map<PrefabKey, YamlNode>): ResolvedYaml {
        val resolved = mutableMapOf<PrefabKey, YamlNode>()
        val errors = mutableMapOf<PrefabKey, String>()
        var pending = files.keys.toList()

        while (pending.isNotEmpty()) {
            val waiting = mutableMapOf<PrefabKey, PrefabKey>()
            for (key in pending) {
                try {
                    resolved[key] = resolve(files.getValue(key), resolved::get)
                } catch (missing: MissingBase) {
                    when {
                        missing.key in errors -> errors[key] = "copies ${missing.key}, which failed to load"
                        missing.key in files -> waiting[key] = missing.key
                        else -> errors[key] = "copies ${missing.key}, which does not exist"
                    }
                } catch (e: YamlLoadException) {
                    errors[key] = e.message ?: "invalid file"
                }
            }
            if (waiting.size == pending.size) {
                waiting.forEach { (key, base) -> errors[key] = "copies $base, which never resolved, check for files copying each other" }
                break
            }
            pending = waiting.keys.toList()
        }
        return ResolvedYaml(resolved, errors)
    }

    fun resolveLoaded(node: YamlNode, resolved: (PrefabKey) -> YamlNode?): YamlNode = try {
        resolve(node, resolved)
    } catch (missing: MissingBase) {
        throw YamlLoadException("Copies ${missing.key}, which does not exist")
    }

    private fun resolve(node: YamlNode, resolved: (PrefabKey) -> YamlNode?): YamlNode = when (node) {
        is YamlList -> YamlList(node.items.map { resolve(it, resolved) }, node.path)
        is YamlTaggedNode -> node.copy(innerNode = resolve(node.innerNode, resolved))
        is YamlMap -> {
            val entries = node.entries.mapValues { (_, value) -> resolve(value, resolved) }
            val inheritNode = node.get<YamlNode>(INHERIT)
            if (inheritNode == null) YamlMap(entries, node.path)
            else {
                val where = inheritNode.path.toHumanReadableString()
                val keyString = (inheritNode as? YamlScalar)?.content ?: throw YamlLoadException("inherit must be a key at $where")
                val key = PrefabKey.ofOrNull(keyString) ?: throw YamlLoadException("Invalid key '$keyString' at $where")
                val base = resolved(key) ?: throw MissingBase(key)
                deepMerge(base.withoutTemplateFlag(), YamlMap(entries.filterKeys { it.content != INHERIT }, node.path))
            }
        }

        else -> node
    }

    private fun YamlNode.withoutTemplateFlag(): YamlNode =
        if (this is YamlMap) YamlMap(entries.filterKeys { it.content != TEMPLATE }, path) else this

    private fun deepMerge(base: YamlNode, override: YamlNode): YamlNode {
        if (base !is YamlMap || override !is YamlMap) return override
        val merged = LinkedHashMap<String, Pair<YamlScalar, YamlNode>>()
        base.entries.forEach { (key, value) -> merged[key.content] = key to value }
        override.entries.forEach { (key, value) ->
            val existing = merged[key.content]?.second
            merged[key.content] = key to (if (existing != null) deepMerge(existing, value) else value)
        }
        return YamlMap(merged.values.toMap(), override.path)
    }
}
