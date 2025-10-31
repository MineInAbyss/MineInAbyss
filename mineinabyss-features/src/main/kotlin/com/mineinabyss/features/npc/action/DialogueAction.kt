package com.mineinabyss.features.npc.action

import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

fun customAction(player: Player) {
    player.sendMessage("Custom action executed!")
}
@Serializable
sealed class DialogueAction_ {
//    @Serializable
//    @SerialName("dialogue")
//    data class customAction(val player: Player): DialogueAction()
//    val newDialog: Dialogue.Builder
//    val answer1: Answer.Builder
//    val answer2: Answer.Builder
//    val dialog: Dialogue
//    val page1: Page.Builder = Page.Builder()
//    val texts = listOf(
//        "first line",
//        "second line",
//        "third line"
//    )
//    init {
//        newDialog = Dialogue.Builder()
//            .setDialogueID("dialog1")
//            .setDialogueSpeed(1)
//            .setTypingSound("typingsound")
//            .setTypingSoundPitch(1.0)
//            .setTypingSoundVolume(1.0)
//            .setRange(2.0)
//            .setSelectionSound("selectionsound")
//            .setEffect("Slowness")
//            .setAnswerNumbers(true)
//            .setArrowImage("hand", "#4f4a3e", -7)
//            .setDialogueBackgroundImage("dialogue-background", "#f8ffe0", 0)
//            .setAnswerBackgroundImage("answer-background", "#f8ffe0", 140)
//            .setDialogueText("#4f4a3e", 10)
//            .setAnswerText("#4f4a3e", 13, "#4f4a3e")
//            .setCharacterImage("character-background", -16)
//            .setCharacterNameText("charactername", "#4f4a3e", 20)
//            .setNameStartImage("name-start")
//            .setNameMidImage("name-mid")
//            .setNameEndImage("name-end")
//            .setNameImageColor("#f8ffe0")
//            .setFogImage("fog", "#000000")
//
//
//        answer1 = Answer.Builder()
//            .setAnswerID("answer1")
//            .setAnswerText("Hello!")
//            .setSoundName("answer1sound")
//            .addReplyMessage("replymessage")
//            .addAction("msg %player_name% hello")
//            .addCondition("%balance%>=5")
//
//        answer2 = Answer.Builder()
//            .setAnswerID("answer2")
//            .setAnswerText("Goodbye!")
//            .setSoundName("answer2sound")
//            .addReplyMessage("replymessage")
//         .addCallback { player -> customAction(player) }
//
//
//        texts.forEach { line ->
//            page1.addLine(line)
//        }
//        page1.addPreAction("say pre action")
//        page1.addPostAction("say post action")
//        newDialog.addPage(page1.build())
//        dialog = newDialog
//            .addAnswer(answer1.build())
//            .addAnswer(answer2.build())
//            .build()
//
//
//    }

//    fun openDialogue(player: Player) {4
//        LuxDialoguesAPI.getProvider().sendDialogue(player, dialog)
//    }
}