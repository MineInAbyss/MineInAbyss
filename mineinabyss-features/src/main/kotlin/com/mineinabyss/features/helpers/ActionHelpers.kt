package com.mineinabyss.features.helpers

import com.mineinabyss.features.abyss
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.ActionsCancelledException
import com.mineinabyss.geary.actions.Tasks
import com.mineinabyss.geary.actions.execute
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import org.bukkit.Bukkit
import org.bukkit.entity.Player

private const val PLAYER_KEY = "player"

fun actionContext(player: Player, environment: Map<String, Any?> = emptyMap()) =
    ActionGroupContext(player.toGeary()).apply {
        environment.forEach { (key, value) -> register(key, value) }
        register(PLAYER_KEY, player)
    }

val ActionGroupContext.player: Player? get() = environment[PLAYER_KEY] as? Player ?: entity?.get<Player>()

fun Tasks.runIn(context: ActionGroupContext) = runCatching {
    execute(context)
}.onFailure {
    if (it !is ActionsCancelledException) abyss.logger.e("Dialog action failed: ${it.message}", it)
}

inline fun <reified T> Tasks?.actionsOf(): List<T> = this?.tasks.orEmpty().mapNotNull { it.action as? T }

fun nextTick(block: () -> Unit) {
    Bukkit.getScheduler().runTask(abyss, Runnable(block))
}
