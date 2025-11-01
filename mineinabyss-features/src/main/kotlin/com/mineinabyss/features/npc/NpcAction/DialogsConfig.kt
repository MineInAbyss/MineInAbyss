package com.mineinabyss.features.npc.NpcAction

import com.mineinabyss.features.abyss
import com.mineinabyss.features.npc.Npc
import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Task
import com.mineinabyss.geary.actions.Tasks
import com.mineinabyss.geary.actions.execute
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.messaging.error
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.aselstudios.luxdialoguesapi.Builders.Answer
import org.aselstudios.luxdialoguesapi.Builders.Dialogue
import org.aselstudios.luxdialoguesapi.Builders.Page
import org.aselstudios.luxdialoguesapi.LuxDialoguesAPI
import org.bukkit.entity.Player

//@Serializable
//@SerialName("mineinabyss:custom_action")
//class CustomAction : Action {
//    override fun ActionGroupContext.execute() {
//        entity?.get<Player>()?.sendMessage("Custom action executed!")
//    }
//}
//
//// TODO: pass ticket id in the action serializer so we can tie unlocks to actions in config and avoid dealing with npc holding ticket id and such
//@Serializable
//@SerialName("mineinabyss:unlock_gondola_action")
//class gondolaUnlockerInteractionAction : Action {
//    override fun ActionGroupContext.execute() {
//        val player = entity?.get<Player>() ?: return
//        val npc = environment["npc"] as? Npc ?: return
//        npc.gondolaUnlockerInteraction(player)
//    }
//}
//@Serializable
//@SerialName("mineinabyss:unlock_quest_action")
//class unlockQuestAction(val questId: String) : Action {
//    override fun ActionGroupContext.execute() {
//        val player = entity?.get<Player>() ?: return
//        val npc = environment["npc"] as? Npc ?: return
//        npc.questUnlockInteraction(player, questId)
//    }
//}
//
//
//@Serializable
//@SerialName("mineinabyss:complete_quest_action")
//class completeQuestAction(val questId: String) : Action {
//    override fun ActionGroupContext.execute() {
//        val player = entity?.get<Player>() ?: return
//        val npc = environment["npc"] as? Npc ?: return
//        npc.questCompleteInteraction(player, questId)
//    }
//}

@Serializable
data class DialogueAction(
    val name: String,
) {
    fun customAction(player: Player) {
        player.sendMessage("Custom action executed!")
    }


    fun execute(player: Player, npc: Npc) {
        val plugin = abyss.plugin // Adjust if plugin access differs
        when (name) {
            "customAction" -> plugin.server.scheduler.runTask(plugin, Runnable { customAction(player) })
            "gondolaAction" -> plugin.server.scheduler.runTask(plugin, Runnable { npc.gondolaUnlockerInteraction(player) })
            "unlockQuestAction" -> plugin.server.scheduler.runTask(plugin, Runnable { npc.questUnlockInteraction(player, npc.questId!!) })
            "completeQuestAction" -> plugin.server.scheduler.runTask(plugin, Runnable { npc.questCompleteInteraction(player, npc.questId!!) })
            else -> player.error("Error resolving action: $name")
        }
    }

}



@Serializable
class AnswerData(
    val text: String,
    @EncodeDefault(Mode.NEVER)
    val placeholderCondition: String? = null,
    @EncodeDefault(Mode.NEVER)
    val replyMessage: String? = null,
    @EncodeDefault(Mode.NEVER)
    val action: DialogueAction? = null
) {
    val npc = null
    fun build(npc: Npc): Answer? {
        val answer = Answer.Builder()
            .setAnswerText(text)
        if (placeholderCondition != null) answer.addCondition(placeholderCondition)
        if (replyMessage != null) answer.addReplyMessage(replyMessage)
        if (action != null) answer.addCallback { player -> action.execute(player, npc) }
        return answer.build()
    }
}

@Serializable
class PageData(
    val lines: List<String> = emptyList(),
    @EncodeDefault(Mode.NEVER)
    val preAction: String? = null,
    @EncodeDefault(Mode.NEVER)
    val postAction: String? = null,
) {

    fun build(): Page? {
        val page = Page.Builder()
        lines.forEach { line -> page.addLine(line) }
        if (preAction != null) page.addPreAction(preAction)
        if (postAction != null) page.addPostAction(postAction)
        return page.build()
    }
}

@Serializable
class DialogData(
    @EncodeDefault(Mode.NEVER) val typingSpeed: Int = 1,
    @EncodeDefault(Mode.NEVER) val typingSound: String = "luxdialogues:luxdialogues.sounds.typing",
    @EncodeDefault(Mode.NEVER) val selectionSound : String = "luxdialogues:luxdialogues.sounds.selection",
    @EncodeDefault(Mode.NEVER) val range: Double = 3.0,
    @EncodeDefault(Mode.NEVER) val effect: String = "Slowness",
    @EncodeDefault(Mode.NEVER) val answerNumbers: Boolean = true,
    @EncodeDefault(Mode.NEVER) val dialogueTextColor: String = "#4f4a3e",
    @EncodeDefault(Mode.NEVER) val backgroundFog: Boolean = true,
    @EncodeDefault(Mode.NEVER) val characterName : String = "default name",
    @EncodeDefault(Mode.NEVER) val characterNameColor: String = "#4f4a3e",
    @EncodeDefault(Mode.NEVER) val characterImage: String = "character-background",
    @EncodeDefault(Mode.NEVER) val nameStartImage: String = "name-start",
    @EncodeDefault(Mode.NEVER) val nameMidImage: String = "name-mid",
    @EncodeDefault(Mode.NEVER) val nameEndImage: String = "name-end",
    @EncodeDefault(Mode.NEVER) val answerBackgroundImage: String = "answer-background",
    @EncodeDefault(Mode.NEVER) val dialogueBackgroundImage: String = "dialogue-background",
    @EncodeDefault(Mode.NEVER) val dialogBackgroundImageColor: String = "#f8ffe0",
    @EncodeDefault(Mode.NEVER) val answerBackgroundImageColor: String = "#f8ffe0",
    @EncodeDefault(Mode.NEVER) val cursorIconImage: String = "hand",
    @EncodeDefault(Mode.NEVER) val answers: List<AnswerData> = emptyList(),
    @EncodeDefault(Mode.NEVER) val pages: List<PageData> = emptyList(),
) {

    fun build(id: String, npc: Npc): Dialogue? {
        val dialogue = Dialogue.Builder()
            .setDialogueID(id)
            .setDialogueSpeed(typingSpeed)
            .setTypingSound(typingSound)
            .setTypingSoundPitch(1.0)
            .setTypingSoundVolume(1.0)
            .setRange(range)
            .setSelectionSound(selectionSound)
            .setEffect(effect)
            .setAnswerNumbers(answerNumbers)
            .setArrowImage(cursorIconImage, "#4f4a3e", -7)
            .setDialogueBackgroundImage(dialogueBackgroundImage, dialogBackgroundImageColor, 0)
            .setAnswerBackgroundImage(answerBackgroundImage, answerBackgroundImageColor, 140)
            .setDialogueText(dialogueTextColor, 10)
            .setAnswerText(dialogueTextColor, 13, dialogueTextColor)
            .setCharacterImage(characterImage, -16)
            .setCharacterNameText(characterName, characterNameColor, 20)
            .setNameStartImage(nameStartImage)
            .setNameMidImage(nameMidImage)
            .setNameEndImage(nameEndImage)
            .setNameImageColor("#f8ffe0")

        if (backgroundFog) dialogue.setFogImage("fog", "#000000")

        answers.forEach { answerData -> dialogue.addAnswer(answerData.build(npc)) }
        pages.forEach { pageData -> dialogue.addPage(pageData.build()) }
        return dialogue.build()
    }

    fun startDialogue(player: Player, id: String, npc: Npc) {
        val dialog = this.build(id, npc) ?: return
        LuxDialoguesAPI.getProvider().sendDialogue(player, dialog)
    }
}

@Serializable
data class QuestDialogData(
    val dialogData: DialogData,
)

@Serializable
class DialogsConfig(
    val configs: Map<String, DialogData> = mapOf()
)