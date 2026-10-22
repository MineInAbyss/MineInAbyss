package com.mineinabyss.features.dialogs.model

import com.mineinabyss.features.dialogs.conditions.DialogCondition
import com.mineinabyss.features.dialogs.conditions.DialogConditionsSerializer
import com.mineinabyss.features.helpers.KeyOrInline
import com.mineinabyss.features.helpers.KeyOrInlineSerializer
import com.mineinabyss.geary.actions.Tasks
import com.mineinabyss.geary.autoscan.ExcludeAutoScan
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.StringsSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** [speaker], [style] and [focus] apply to this node and everything after it until overridden, [focus] is an npc key the player is kept facing */
@Serializable
@ExcludeAutoScan
sealed interface DialogNode {
    val speaker: String?
    val style: String?
    val focus: PrefabKey?

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
    override val focus: PrefabKey? = null,
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
    override val focus: PrefabKey? = null,
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
    override val focus: PrefabKey? = null,
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
    override val focus: PrefabKey? = null,
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

/**
 * A scalar is a reference to another dialog, looked up when reached so dialogs may loop back.
 * A map is an inline dialog. Template copies were already expanded into inline maps before decoding
 */
typealias DialogRef = @Serializable(DialogRefSerializer::class) KeyOrInline<DialogNode>

object DialogRefSerializer : KeyOrInlineSerializer<DialogNode>(DialogNode::serializer)
