package com.mineinabyss.features.dialogs

import kotlinx.serialization.Serializable
import org.aselstudios.luxdialoguesapi.Builders.Dialogue
import org.bukkit.SoundCategory

// Layout values from the Offsets, Images and Colors sections of LuxDialogues' Dialogues/default_example.yml
private const val ARROW_OFFSET = -7
private const val DIALOGUE_BACKGROUND_OFFSET = 0
private const val ANSWER_BACKGROUND_OFFSET = 140
private const val DIALOGUE_LINE_OFFSET = 10
private const val ANSWER_LINE_OFFSET = 13
private const val CHARACTER_OFFSET = -16
private const val NAME_OFFSET = 0
private const val NAME_BACKGROUND_OFFSET = 20
private const val FOG_IMAGE = "fog"
private const val FOG_COLOR = "#000000"

@Serializable
class DialogsConfig(
    /** Lines LuxDialogues fits in one box, see `Pack/Lines/lines.yml` in LuxDialogues */
    val linesPerPage: Int = 5,
    val defaultStyle: String = "default",
    val styles: Map<String, DialogStyle> = mapOf("default" to DialogStyle()),
    val denySound: String = "minecraft:entity.villager.no",
) {
    fun style(name: String?): DialogStyle = styles[name ?: defaultStyle] ?: styles[defaultStyle] ?: DialogStyle()
}

@Serializable
class DialogStyle(
    val typingSpeed: Int = 1,
    val typingSound: String = "luxdialogues:luxdialogues.sounds.typing",
    val selectionSound: String = "luxdialogues:luxdialogues.sounds.selection",
    val range: Double = 3.0,
    val effect: String = "Slowness",
    val answerNumbers: Boolean = false,
    val npcFocus: Boolean = false,
    val textColor: String = "#4f4a3e",
    val lockedTextColor: String = "#9b9584",
    val backgroundFog: Boolean = true,
    val characterNameColor: String = "#4f4a3e",
    val characterImage: String = "character-background",
    val nameStartImage: String = "name-start",
    val nameMidImage: String = "name-mid",
    val nameEndImage: String = "name-end",
    val nameBackgroundColor: String = "#f8ffe0",
    val answerBackgroundImage: String = "answer-background",
    val dialogueBackgroundImage: String = "dialogue-background",
    val dialogueBackgroundColor: String = "#f8ffe0",
    val answerBackgroundColor: String = "#f8ffe0",
    val cursorIconImage: String = "hand",
) {
    fun builder(id: String, speaker: String): Dialogue.Builder = Dialogue.Builder()
        .setDialogueID(id)
        .setDialogueSpeed(typingSpeed)
        .setTypingSound(typingSound, SoundCategory.UI.name, 1.0, 1.0)
        .setSelectionSound(selectionSound, SoundCategory.UI.name, 1.0, 1.0)
        .setRange(range)
        .setEffect(effect)
        .setAnswerNumbers(answerNumbers)
        .setNpcFocus(npcFocus)
        .setArrowImage(cursorIconImage, textColor, ARROW_OFFSET)
        .setDialogueBackgroundImage(dialogueBackgroundImage, dialogueBackgroundColor, DIALOGUE_BACKGROUND_OFFSET)
        .setAnswerBackgroundImage(answerBackgroundImage, answerBackgroundColor, ANSWER_BACKGROUND_OFFSET)
        .setDialogueText(textColor, DIALOGUE_LINE_OFFSET)
        .setAnswerText(textColor, ANSWER_LINE_OFFSET, textColor)
        .setCharacterImage(characterImage, "white", CHARACTER_OFFSET)
        .setCharacterNameText(speaker, characterNameColor, NAME_OFFSET)
        .setNameImage(nameStartImage, nameMidImage, nameEndImage, nameBackgroundColor, NAME_BACKGROUND_OFFSET)
        .apply { if (backgroundFog) setFogImage(FOG_IMAGE, FOG_COLOR) }
}
