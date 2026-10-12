package com.mineinabyss.features.dialogs.conditions

import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.yamlMap
import com.mineinabyss.features.abyss
import com.mineinabyss.features.dialogs.runtime.session
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.ActionsCancelledException
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.actions.execute
import com.mineinabyss.geary.autoscan.ExcludeAutoScan
import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.serialization.ComponentSerializers.Companion.fromCamelCaseToSnakeCase
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.serialization.getWorld
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Written as a map with one condition key and an optional `expect`.
 * The condition passes when its result equals [expect], so `expect: false` inverts it.
 * Bare keys resolve to `mineinabyss:` first, then `geary:`
 */
@Serializable(with = DialogCondition.Serializer::class)
@ExcludeAutoScan
class DialogCondition(val condition: Condition, val expect: Boolean = true) {
    fun check(context: ActionGroupContext): Boolean = try {
        condition.execute(context) == expect
    } catch (_: ActionsCancelledException) {
        !expect
    } catch (e: Exception) {
        abyss.logger.e("Condition $condition in dialog ${context.session?.id ?: "none"} failed: ${e.message}")
        false
    }

    object Serializer : KSerializer<DialogCondition> {
        private const val EXPECT = "expect"
        private val defaultNamespaces = listOf("mineinabyss", "geary")

        override val descriptor: SerialDescriptor = ContextualSerializer(Any::class).descriptor

        override fun deserialize(decoder: Decoder): DialogCondition {
            val input = decoder as? YamlInput ?: throw SerializationException("Conditions can only be read from yaml")
            val map = input.node.yamlMap
            val expect = map.getScalar(EXPECT)?.toBoolean() ?: true
            val (key, value) = map.entries.entries.singleOrNull { it.key.content != EXPECT }
                ?: throw SerializationException("A condition needs exactly one condition key at ${map.path.toHumanReadableString()}")

            val formats = decoder.serializersModule.getWorld().getAddon(SerializableComponents).formats
            val name = key.content.fromCamelCaseToSnakeCase()
            val candidates = if (":" in name) listOf(name) else defaultNamespaces.map { "$it:$name" }
            val serializer = candidates.firstNotNullOfOrNull { formats.getSerializerFor(it, GearyComponent::class) }
                ?: throw SerializationException("Unknown condition '${key.content}' at ${key.path.toHumanReadableString()}")

            val condition = input.yaml.decodeFromYamlNode(serializer, value) as? Condition
                ?: throw SerializationException("'${key.content}' is not a condition at ${key.path.toHumanReadableString()}")
            return DialogCondition(condition, expect)
        }

        override fun serialize(encoder: Encoder, value: DialogCondition) =
            throw SerializationException("Conditions cannot be written back")
    }
}

fun List<DialogCondition>.allPass(context: ActionGroupContext) = all { it.check(context) }
