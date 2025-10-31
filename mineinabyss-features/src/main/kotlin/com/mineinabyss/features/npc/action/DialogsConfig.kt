package com.mineinabyss.features.npc.action

import com.mineinabyss.features.npc.Npc
import com.mineinabyss.idofront.messaging.error
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import org.aselstudios.luxdialoguesapi.Builders.Answer
import org.aselstudios.luxdialoguesapi.Builders.Dialogue
import org.aselstudios.luxdialoguesapi.Builders.Page
import org.aselstudios.luxdialoguesapi.LuxDialoguesAPI
import org.bukkit.entity.Player

@Serializable
data class DialogueAction(
    val name: String,
) {
    fun customAction(player: Player) {
        player.sendMessage("Custom action executed!")
    }

    fun execute(player: Player, npc: Npc) {
        when (name) {
            "customAction" -> customAction(player)
//            "gondolaAction" ->
            else -> player.error("Error resolving action: $name")
        }
    }
}

@Serializable
class AnswerData(
    val text: String,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val placeholderCondition: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val replyMessage: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val action: DialogueAction? = null
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
    @EncodeDefault(EncodeDefault.Mode.NEVER) val lines: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.NEVER) val preAction: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val postAction: String? = null,
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
    val typingSpeed: Int = 1,
    val typingSound: String = "luxdialogues:luxdialogues.sounds.typing",
    val selectionSound : String = "luxdialogues:luxdialogues.sounds.selection",
    val range: Double = 3.0,
    val effect: String = "Slowness",
    val answerNumbers: Boolean = true,
    val dialogueTextColor: String = "#4f4a3e",
    val backgroundFog: Boolean = true,
    val characterName : String = "default name",
    val characterNameColor: String = "#4f4a3e",
    val characterImage: String = "character-background",
    val nameStartImage: String = "name-start",
    val nameMidImage: String = "name-mid",
    val nameEndImage: String = "name-end",
    val answerBackgroundImage: String = "answer-background",
    val dialogueBackgroundImage: String = "dialogue-background",
    val dialogBackgroundImageColor: String = "#f8ffe0",
    val answerBackgroundImageColor: String = "#f8ffe0",
    val cursorIconImage: String = "hand",
    @EncodeDefault(EncodeDefault.Mode.NEVER) val answers: List<AnswerData> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.NEVER) val pages: List<PageData> = emptyList(),
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
@JvmInline
value class DialogsConfig(val configs: Map<String, DialogData> = mapOf())