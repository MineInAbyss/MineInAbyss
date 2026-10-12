package com.mineinabyss.features.dialogs.runtime

import com.mineinabyss.features.abyss
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.ActionsCancelledException
import com.mineinabyss.geary.actions.Tasks
import com.mineinabyss.geary.actions.execute
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import org.bukkit.Bukkit
import org.bukkit.entity.Player

private const val PLAYER_KEY = "player"
const val SESSION_KEY = "session"

const val SPEAKER_KEY = "speaker"
const val STYLE_KEY = "dialog_style"

fun dialogContext(player: Player, environment: Map<String, Any?> = emptyMap()) =
    ActionGroupContext(player.toGeary()).apply {
        environment.forEach { (key, value) -> register(key, value) }
        register(PLAYER_KEY, player)
    }

val ActionGroupContext.player: Player? get() = environment[PLAYER_KEY] as? Player ?: entity?.get<Player>()

val ActionGroupContext.session: DialogSession? get() = environment[SESSION_KEY] as? DialogSession

fun Tasks.runIn(context: ActionGroupContext) {
    try {
        execute(context)
    } catch (_: ActionsCancelledException) {
    } catch (e: Exception) {
        abyss.logger.e("Dialog action failed: ${e.message}", e)
    }
}

internal fun nextTick(block: () -> Unit) {
    Bukkit.getScheduler().runTask(abyss, Runnable(block))
}

// LuxDialogues runs answer callbacks off the main thread
internal fun onMainThread(block: () -> Unit) {
    if (Bukkit.isPrimaryThread()) block() else nextTick(block)
}
