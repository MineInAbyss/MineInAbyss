package com.mineinabyss.features.dialogs.loading

import com.charleskorn.kaml.*
import com.mineinabyss.geary.prefabs.PrefabKey

class DialogLoadException(message: String) : Exception(message)

private class MissingBase(val key: PrefabKey) : Exception()

class ResolvedDialogs(val nodes: Map<PrefabKey, YamlNode>, val errors: Map<PrefabKey, String>)

/**
 * A map with `inherit: ns:key` is a copy of that dialog with the map's other keys deep-merged over it.
 * Maps merge per key, lists and scalars replace.
 * `inherit` must be a literal key, variables are only filled in after every copy is resolved
 */
object DialogInheritance {
    const val INHERIT = "inherit"

    fun resolveAll(files: Map<PrefabKey, YamlNode>): ResolvedDialogs {
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
                } catch (e: DialogLoadException) {
                    errors[key] = e.message ?: "invalid dialog"
                }
            }
            if (waiting.size == pending.size) {
                waiting.forEach { (key, base) -> errors[key] = "copies $base, which never resolved, check for dialogs copying each other" }
                break
            }
            pending = waiting.keys.toList()
        }
        return ResolvedDialogs(resolved, errors)
    }

    fun resolveLoaded(node: YamlNode, resolved: (PrefabKey) -> YamlNode?): YamlNode = try {
        resolve(node, resolved)
    } catch (missing: MissingBase) {
        throw DialogLoadException("Copies ${missing.key}, which does not exist")
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
                val keyString = (inheritNode as? YamlScalar)?.content ?: throw DialogLoadException("inherit must be a dialog key at $where")
                val key = PrefabKey.ofOrNull(keyString) ?: throw DialogLoadException("Invalid dialog key '$keyString' at $where")
                val base = resolved(key) ?: throw MissingBase(key)
                deepMerge(base, YamlMap(entries.filterKeys { it.content != INHERIT }, node.path))
            }
        }

        else -> node
    }

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
