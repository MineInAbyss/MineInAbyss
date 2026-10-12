package com.mineinabyss.features.dialogs.runtime

import com.mineinabyss.features.abyss
import com.mineinabyss.features.dialogs.conditions.allPass
import com.mineinabyss.features.dialogs.model.*
import com.mineinabyss.geary.actions.Tasks
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.aselstudios.luxdialoguesapi.Builders.Answer as LuxAnswer
import org.aselstudios.luxdialoguesapi.Builders.Dialogue
import org.aselstudios.luxdialoguesapi.Builders.Page
import org.aselstudios.luxdialoguesapi.DialogueProvider
import org.aselstudios.luxdialoguesapi.LuxDialoguesAPI
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.lang.reflect.Method
import java.util.Collections
import java.util.IdentityHashMap

/**
 * Walks a dialog tree for one player and feeds it to LuxDialogues one segment at a time.
 *
 * A segment is every page reachable without player input: PAGE chains are linked with Lux `goTo`,
 * CONDITIONALs are evaluated while the segment is built, and the segment stops at a REPLY.
 * ACTION nodes run while the segment is built, or when the player clicks past the page before them.
 * Answers call back into the session, which builds the next segment.
 * Speaker and style are read when a segment starts, so a change only shows after the next answer
 */
class DialogSession(
    val player: Player,
    val id: String,
    private val sessions: DialogSessions,
    private var speaker: String,
    private var style: String?,
    environment: Map<String, Any?>,
    private val focus: Entity?,
) {
    val context = dialogContext(player, environment + (SESSION_KEY to this))
    private val config get() = sessions.config
    private var closed = false
    private var generation = 0
    private val isDetached get() = closed || !sessions.isCurrentOrIdle(this)

    internal var dialogue: Dialogue? = null
        private set

    fun show(node: DialogNode, runActions: Boolean = true) {
        if (closed) return
        node.speaker?.let { speaker = it }
        node.style?.let { style = it }

        val segment = Segment(runActions, ++generation).apply { build(node) }
        // An ACTION node may have opened another dialog while the segment was built
        if (isDetached) return
        val first = segment.pages.firstOrNull() ?: return close()

        val dialogue = config.style(style).builder(luxId(), speaker)
            .setExternallyDriven()
            .apply { segment.pages.forEach { addPage(it.build()) } }
            .build()
        this.dialogue = dialogue

        sessions.register(this)
        val provider = LuxDialoguesAPI.getProvider()
        if (provider.isInDialogue(player) == true) provider.redirectDialogue(player, dialogue, first.id)
        else provider.sendDialogue(player, dialogue, first.id)
        // Lux drops any focus whenever it sends a dialogue, so it is applied again for every segment
        focus?.let { provider.focusEntity(player, it) }
    }

    /** @param clearBox false when another session takes over the open box */
    fun close(clearBox: Boolean = true) {
        if (closed) return
        closed = true
        sessions.unregister(this)
        val provider = LuxDialoguesAPI.getProvider()
        if (clearBox && provider.isInDialogue(player) == true) provider.clearDialogue(player)
    }

    // Without the fork Lux stops the box on every answer click, before the answer callback has run
    internal fun onLuxStopped() {
        if (externallyDrivenSetter == null) sessions.unregister(this) else close(clearBox = false)
    }

    private fun resolve(ref: DialogRef?): DialogNode? = when (ref) {
        null -> null
        is DialogRef.Inline -> ref.node
        is DialogRef.Reference -> sessions.registry[ref.key].also {
            if (it == null) abyss.logger.w("Dialog $id points at missing dialog ${ref.key}")
        }
    }

    private fun isActive(generation: Int) = !isDetached && generation == this.generation

    // Lux can run an answer's callback twice on a quick double click, so the first one retires the segment
    private fun onAnswer(reply: ReplyNode, answer: Answer, generation: Int) = onMainThread {
        if (!player.isOnline || !isActive(generation)) return@onMainThread
        this.generation++
        if (!answer.conditions.allPass(context)) {
            player.playSound(Sound.sound(Key.key(config.denySound), Sound.Source.MASTER, 1f, 1f))
            return@onMainThread show(reply, runActions = false)
        }
        answer.actions?.runIn(context)
        // Actions may have closed this dialog or opened another one
        if (isDetached) return@onMainThread
        resolve(answer.next)?.let { show(it) } ?: close()
    }

    private fun continueAfter(node: ActionNode) {
        if (isDetached) return
        node.actions?.runIn(context)
        if (isDetached) return
        resolve(node.next)?.let { show(it) } ?: close()
    }

    private fun luxId() = "mineinabyss_" + id.replace(':', '_')

    private class PageDraft(val id: String, val lines: List<String>) {
        var goTo: String? = null
        var preAction: (() -> Unit)? = null
        val answers = mutableListOf<LuxAnswer>()
        /** Counted from 1 like Lux does */
        var defaultAnswer: Int? = null

        fun build(): Page = Page.Builder().setID(id).apply {
            lines.forEach { addLine(it) }
            goTo?.let { setGoTo(listOf(it)) }
            // Lux keeps setting the page up after these return, so an action closing the dialog in place would leave the box drawn
            preAction?.let { action -> addPreCallback { nextTick(action) } }
            answers.forEach { addAnswer(it) }
            defaultAnswer?.let { setDefaultAnswer(it) }
        }.build()
    }

    private inner class Segment(private val runActions: Boolean, private val generation: Int) {
        val pages = mutableListOf<PageDraft>()
        private val pageIds = IdentityHashMap<DialogNode, String>()
        private val visitedNonPages = Collections.newSetFromMap(IdentityHashMap<DialogNode, Boolean>())
        private var previous: PageDraft? = null

        fun build(start: DialogNode) {
            var current: DialogNode? = start
            while (current != null) {
                val node = current
                pageIds[node]?.let { existing ->
                    previous?.goTo = existing
                    return
                }
                current = when (node) {
                    is ConditionalNode -> {
                        if (!visitedNonPages.add(node)) return
                        resolve(if (node.conditions.allPass(context)) node.onTrue else node.onFalse)
                    }

                    is PageNode -> {
                        addPages(node, node.lines, node.actions)
                        resolve(node.next)
                    }

                    is ReplyNode -> {
                        val page = addPages(node, node.lines, node.actions)
                        val shown = shownAnswers(node)
                        // With nothing selectable the player could not leave the reply, so locked answers stay clickable and get turned away in onAnswer
                        val disableLocked = shown.any { it.unlocked }
                        page.answers += shown.map { it.toLux(node, disableLocked) }
                        page.defaultAnswer = defaultAnswer(shown)
                        null
                    }

                    is ActionNode -> {
                        val last = previous
                        if (last != null) {
                            continueOnDismiss(last, node)
                            return
                        }
                        if (!visitedNonPages.add(node)) return
                        if (runActions) node.actions?.runIn(context)
                        if (isDetached) return
                        resolve(node.next)
                    }
                }
            }
        }

        // Lux only advances a page on the player's click when it has a page to go to, so a blank one is added and runs the action as it opens
        private fun continueOnDismiss(page: PageDraft, node: ActionNode) {
            val dismissed = PageDraft("p${pages.size}", listOf(" ")).also(pages::add)
            page.goTo = dismissed.id
            dismissed.preAction = whileActive { continueAfter(node) }
        }

        private fun addPages(node: DialogNode, lines: List<String>, actions: Tasks?): PageDraft {
            val chunks = lines.ifEmpty { listOf(" ") }.chunked(config.linesPerPage.coerceAtLeast(1))
            val drafts = chunks.map { PageDraft("p${pages.size}", it).also(pages::add) }
            val first = drafts.first()
            pageIds[node] = first.id
            previous?.goTo = first.id
            drafts.zipWithNext { a, b -> a.goTo = b.id }
            if (runActions && actions != null) first.preAction = whileActive { actions.runIn(context) }
            previous = drafts.last()
            return drafts.last()
        }

        private fun whileActive(action: () -> Unit): () -> Unit = { if (isActive(generation)) action() }

        private fun shownAnswers(reply: ReplyNode): List<ShownAnswer> = reply.answers.withIndex()
            .map { (index, answer) -> ShownAnswer(index, answer, answer.conditions.allPass(context)) }
            .filter { it.unlocked || it.answer.whenLocked != LockedAnswer.HIDE }

        private fun ShownAnswer.toLux(reply: ReplyNode, disableLocked: Boolean): LuxAnswer {
            val text = if (unlocked) answer.text else lockedText(answer.lockedText ?: "${answer.text} (locked)")
            return LuxAnswer.Builder()
                .setAnswerID("a$index")
                .setAnswerText(text)
                .apply { answer.info?.let { setInfoLine(it) } }
                .apply { if (!unlocked && disableLocked) disable() }
                .addCallback { onAnswer(reply, answer, generation) }
                .build()
        }

        private fun defaultAnswer(shown: List<ShownAnswer>): Int? {
            val selected = shown.indexOfFirst { it.unlocked && it.answer.selectedWhen.isNotEmpty() && it.answer.selectedWhen.allPass(context) }
                .takeIf { it >= 0 } ?: shown.indexOfFirst { it.unlocked }
            return selected.takeIf { it >= 0 }?.plus(1)
        }

        private fun lockedText(text: String): String {
            val color = config.style(style).lockedTextColor
            return "<color:$color>$text</color:$color>"
        }
    }

    private class ShownAnswer(val index: Int, val answer: Answer, val unlocked: Boolean)

    private companion object {
        fun forkMethod(lookup: () -> Method): Method? = runCatching(lookup).getOrElse {
            abyss.logger.w("LuxDialogues is missing ${it.message}, dialogs need the 4.1.4-MIA fork to work fully")
            null
        }

        // Older LuxDialogues closes the box between segments, cannot focus entities and always selects the first answer.
        // It also keeps the page's info line, and leaves locked answers selectable so onAnswer turns the click away
        val externallyDrivenSetter = forkMethod { Dialogue.Builder::class.java.getMethod("setExternallyDriven", Boolean::class.javaObjectType) }

        fun Dialogue.Builder.setExternallyDriven() = apply {
            externallyDrivenSetter?.invoke(this, true)
        }

        val focusEntityMethod = forkMethod { DialogueProvider::class.java.getMethod("focusEntity", Player::class.java, Entity::class.java) }

        fun DialogueProvider.focusEntity(player: Player, entity: Entity) {
            focusEntityMethod?.invoke(this, player, entity)
        }

        val defaultAnswerSetter = forkMethod { Page.Builder::class.java.getMethod("setDefaultAnswer", Int::class.javaObjectType) }

        fun Page.Builder.setDefaultAnswer(answer: Int) = apply {
            defaultAnswerSetter?.invoke(this, answer)
        }

        val infoLineSetter = forkMethod { LuxAnswer.Builder::class.java.getMethod("setInfoLine", String::class.java) }

        fun LuxAnswer.Builder.setInfoLine(line: String) = apply {
            infoLineSetter?.invoke(this, line)
        }

        val disabledSetter = forkMethod { LuxAnswer.Builder::class.java.getMethod("setDisabled", Boolean::class.javaPrimitiveType) }

        fun LuxAnswer.Builder.disable() = apply {
            disabledSetter?.invoke(this, true)
        }
    }
}
