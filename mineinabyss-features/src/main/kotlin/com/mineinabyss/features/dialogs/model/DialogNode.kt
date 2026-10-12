package com.mineinabyss.features.dialogs.model

import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlScalar
import com.mineinabyss.features.dialogs.conditions.DialogCondition
import com.mineinabyss.geary.actions.Tasks
import com.mineinabyss.geary.autoscan.ExcludeAutoScan
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.SingleOrListSerializer
import com.mineinabyss.idofront.serialization.StringsSerializer
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** [speaker] and [style] apply to this node and everything after it until overridden */
@Serializable
@ExcludeAutoScan
sealed interface DialogNode {
    val speaker: String?
    val style: String?

    fun children(): List<DialogRef>
}

@Serializable
@ExcludeAutoScan
@SerialName("PAGE")
class PageNode(
    val lines: @Serializable(StringsSerializer::class) List<String> = emptyList(),
    val actions: Tasks? = null,
    val next: DialogRef? = null,
    override val speaker: String? = null,
    override val style: String? = null,
) : DialogNode {
    override fun children() = listOfNotNull(next)
}

@Serializable
@ExcludeAutoScan
@SerialName("CONDITIONAL")
class ConditionalNode(
    val conditions: @Serializable(DialogConditionsSerializer::class) List<DialogCondition> = emptyList(),
    val onTrue: DialogRef? = null,
    val onFalse: DialogRef? = null,
    override val speaker: String? = null,
    override val style: String? = null,
) : DialogNode {
    override fun children() = listOfNotNull(onTrue, onFalse)
}

@Serializable
@ExcludeAutoScan
@SerialName("ACTION")
class ActionNode(
    val actions: Tasks? = null,
    val next: DialogRef? = null,
    override val speaker: String? = null,
    override val style: String? = null,
) : DialogNode {
    override fun children() = listOfNotNull(next)
}

@Serializable
@ExcludeAutoScan
@SerialName("REPLY")
class ReplyNode(
    val lines: @Serializable(StringsSerializer::class) List<String> = emptyList(),
    val answers: List<Answer> = emptyList(),
    val actions: Tasks? = null,
    override val speaker: String? = null,
    override val style: String? = null,
) : DialogNode {
    override fun children() = answers.mapNotNull { it.next }
}

@Serializable
@ExcludeAutoScan
class Answer(
    val text: String,
    val conditions: @Serializable(DialogConditionsSerializer::class) List<DialogCondition> = emptyList(),
    val whenLocked: LockedAnswer = LockedAnswer.SHOW,
    val lockedText: String? = null,
    /** Shown under the dialog text while this answer is selected */
    val info: String? = null,
    /** The first answer whose conditions pass starts selected, the first answer otherwise */
    val selectedWhen: @Serializable(DialogConditionsSerializer::class) List<DialogCondition> = emptyList(),
    val actions: Tasks? = null,
    /** Null ends the dialog after [actions] run */
    val next: DialogRef? = null,
)

enum class LockedAnswer { SHOW, HIDE }

object DialogConditionsSerializer : SingleOrListSerializer<DialogCondition>(DialogCondition.serializer())

/**
 * A scalar is a [Reference] to another dialog, looked up when reached so dialogs may loop back.
 * A map is an [Inline] dialog. Template copies were already expanded into inline maps before decoding
 */
@Serializable(with = DialogRef.Serializer::class)
@ExcludeAutoScan
sealed interface DialogRef {
    data class Reference(val key: PrefabKey) : DialogRef
    data class Inline(val node: DialogNode) : DialogRef

    object Serializer : KSerializer<DialogRef> {
        override val descriptor: SerialDescriptor = ContextualSerializer(Any::class).descriptor

        override fun deserialize(decoder: Decoder): DialogRef {
            val input = decoder as? YamlInput ?: throw SerializationException("Dialogs can only be read from yaml")
            if (input.node is YamlScalar) {
                val key = decoder.decodeString()
                return Reference(PrefabKey.ofOrNull(key) ?: throw SerializationException("Invalid dialog key '$key'"))
            }
            return Inline(DialogNode.serializer().deserialize(decoder))
        }

        override fun serialize(encoder: Encoder, value: DialogRef) =
            throw SerializationException("Dialogs cannot be written back")
    }
}
