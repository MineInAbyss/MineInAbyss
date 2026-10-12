package com.mineinabyss.features.dialogs.loading

import com.charleskorn.kaml.*

/**
 * Fills in `variables` declared on any dialog map, they apply to that map and everything inside it.
 * Inner declarations win over outer ones, and a copy overriding `variables` replaces the values it names.
 * A scalar that is exactly `<name>` becomes the variable's whole node, so numbers, lists and maps keep their type.
 * Other scalars get `<name>` replaced inline.
 * Only declared names are replaced, which leaves MiniMessage tags alone
 */
object DialogVariables {
    const val VARIABLES = "variables"
    private val VARIABLE = Regex("<([A-Za-z0-9_.-]+)>")
    private val WHOLE_VARIABLE = Regex("^<([A-Za-z0-9_.-]+)>$")

    fun substitute(node: YamlNode, scope: Map<String, YamlNode> = emptyMap()): YamlNode = when (node) {
        is YamlScalar -> substituteScalar(node, scope)
        is YamlList -> YamlList(node.items.map { substitute(it, scope) }, node.path)
        is YamlTaggedNode -> node.copy(innerNode = substitute(node.innerNode, scope))
        is YamlMap -> {
            val own = when (val variables = node.get<YamlNode>(VARIABLES)) {
                null -> emptyMap()
                is YamlMap -> variables.entries.entries.associate { (key, value) -> key.content to substitute(value, scope) }
                else -> throw DialogLoadException("variables must be a map at ${variables.path.toHumanReadableString()}")
            }
            val inner = scope + own
            YamlMap(node.entries.filterKeys { it.content != VARIABLES }.mapValues { (_, value) -> substitute(value, inner) }, node.path)
        }

        is YamlNull -> node
    }

    private fun substituteScalar(node: YamlScalar, scope: Map<String, YamlNode>): YamlNode {
        if (scope.isEmpty() || '<' !in node.content) return node
        val whole = WHOLE_VARIABLE.matchEntire(node.content)?.groupValues?.get(1)
        if (whole != null) {
            val value = scope[whole] ?: return node
            // Dropping the name stops `x: <x>` from recursing, other variables used inside the value still resolve here
            return substitute(value.withPath(node.path), scope - whole)
        }
        val replaced = VARIABLE.replace(node.content) { match ->
            val name = match.groupValues[1]
            val value = scope[name] as? YamlScalar ?: return@replace match.value
            (substituteScalar(value, scope - name) as? YamlScalar)?.content ?: match.value
        }
        return if (replaced == node.content) node else node.copy(content = replaced)
    }
}
