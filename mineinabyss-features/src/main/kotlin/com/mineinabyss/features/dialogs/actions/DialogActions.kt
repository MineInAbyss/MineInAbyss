package com.mineinabyss.features.dialogs.actions

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlScalar
import com.mineinabyss.features.abyss
import com.mineinabyss.features.dialogs.DialogRegistry
import com.mineinabyss.features.dialogs.dialogSessions
import com.mineinabyss.features.dialogs.model.DialogNode
import com.mineinabyss.features.dialogs.runtime.SPEAKER_KEY
import com.mineinabyss.features.dialogs.runtime.STYLE_KEY
import com.mineinabyss.features.dialogs.runtime.lock
import com.mineinabyss.features.dialogs.runtime.unlock
import com.mineinabyss.features.helpers.Cost
import com.mineinabyss.features.helpers.actionsOf
import com.mineinabyss.features.helpers.loading.YamlInheritance
import com.mineinabyss.features.helpers.player
import com.mineinabyss.features.quests.QuestManager
import com.mineinabyss.features.quests.questManager
import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.ActionsCancelledException
import com.mineinabyss.geary.actions.Tasks
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.textcomponents.miniMsg
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit

/** [dialog] is a dialog key, or a dialog written in place like in `next` */
@Serializable
@SerialName("mineinabyss:open_dialog")
@JvmInline
value class OpenDialogAction(val dialog: YamlNode) : Action {
    fun resolve(registry: DialogRegistry): Pair<String, DialogNode> {
        if (dialog is YamlScalar) {
            val key = PrefabKey.ofOrNull(dialog.content) ?: throw IllegalArgumentException("Invalid dialog key '${dialog.content}'")
            return key.full to (registry[key] ?: throw IllegalArgumentException("Dialog $key does not exist"))
        }
        val id = (dialog as? YamlMap)?.getScalar(YamlInheritance.INHERIT)?.content ?: "inline"
        return id to registry.inline(dialog)
    }

    override fun ActionGroupContext.execute() {
        val player = player ?: return
        val sessions = dialogSessions ?: return abyss.logger.w("Dialogs feature is not loaded, cannot open a dialog")
        val (id, node) = runCatching { resolve(sessions.registry) }.getOrElse {
            player.error("This dialog is broken, please report it")
            return abyss.logger.e("Could not open dialog at ${dialog.path.toHumanReadableString()}: ${it.message}")
        }
        sessions.start(player, node, id, environment[SPEAKER_KEY] as? String, environment[STYLE_KEY] as? String, environment.toMap())
    }
}

// Dialogs written in place are only decoded when opened, so callers check them on load to report problems early
fun Tasks?.brokenDialogs(): List<String> {
    val registry = dialogSessions?.registry ?: return emptyList()
    return actionsOf<OpenDialogAction>().mapNotNull { action -> runCatching { action.resolve(registry) }.exceptionOrNull()?.message }
}

@Serializable
@SerialName("mineinabyss:close_dialog")
class CloseDialogAction : Action {
    override fun ActionGroupContext.execute() {
        val player = player ?: return
        dialogSessions?.close(player)
    }
}

@Serializable
@SerialName("mineinabyss:message")
@JvmInline
value class MessageAction(val message: String) : Action {
    override fun ActionGroupContext.execute() {
        player?.sendMessage(message.miniMsg())
    }
}

@Serializable
@SerialName("mineinabyss:command")
class CommandAction(val command: String, val asConsole: Boolean = false) : Action {
    override fun ActionGroupContext.execute() {
        val player = player ?: return
        val resolved = command.removePrefix("/").replace("<player>", player.name)
        if (asConsole) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolved)
        else player.performCommand(resolved)
    }
}

/** Stops the actions after it when the player cannot pay */
@Serializable
@SerialName("mineinabyss:pay")
@JvmInline
value class PayAction(val cost: Cost) : Action {
    override fun ActionGroupContext.execute() {
        val player = player ?: return
        if (!cost.take(player)) {
            player.error("You need ${cost.describe()} for this")
            throw ActionsCancelledException()
        }
    }
}

@Serializable
@SerialName("mineinabyss:unlock")
@JvmInline
value class UnlockAction(val key: String) : Action {
    override fun ActionGroupContext.execute() {
        player?.unlock(key)
    }
}

@Serializable
@SerialName("mineinabyss:lock")
@JvmInline
value class LockAction(val key: String) : Action {
    override fun ActionGroupContext.execute() {
        player?.lock(key)
    }
}

@Serializable
@SerialName("mineinabyss:unlock_quest")
@JvmInline
value class UnlockQuestAction(val quest: String) : Action {
    override fun ActionGroupContext.execute() {
        val player = player ?: return
        quests()?.unlockQuest(player, quest)
    }
}

@Serializable
@SerialName("mineinabyss:complete_quest")
@JvmInline
value class CompleteQuestAction(val quest: String) : Action {
    override fun ActionGroupContext.execute() {
        val player = player ?: return
        quests()?.checkAndCompleteQuest(player, quest)
    }
}

private fun quests(): QuestManager? = questManager ?: run {
    abyss.logger.w("Quests feature is not loaded")
    null
}
