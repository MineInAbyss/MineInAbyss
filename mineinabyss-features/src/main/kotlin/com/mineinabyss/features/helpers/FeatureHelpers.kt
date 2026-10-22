package com.mineinabyss.features.helpers

import com.mineinabyss.dependencies.DI
import com.mineinabyss.dependencies.getOrNull
import com.mineinabyss.dependencies.scope
import com.mineinabyss.features.abyss
import com.mineinabyss.features.cinematics.cinematics
import com.mineinabyss.features.dialogs.dialogSessions
import com.mineinabyss.features.transitions.transitions
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.ArgsMinecraft
import com.mineinabyss.idofront.commands.brigadier.context.IdoCommandContext
import com.mineinabyss.idofront.commands.brigadier.map
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.messaging.warn
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

inline fun <reified T : Any> loaded(feature: DI.Module): T? = abyss.scope.getOrNull(feature)?.getOrNull<T>()

fun Player.isInDialog() = dialogSessions?.isInDialog(this) == true

fun Player.isInCinematicOrTransition() = cinematics?.isPlaying(this) == true || transitions?.isInTransition(this) == true

private fun Args.prefabKey() = ArgsMinecraft.key()
    .map { PrefabKey.of(it.namespace(), it.value()) }

/**
 * Suggests keys whose part after the colon starts with what was typed, then those whose full key does.
 * A key typed without its namespace is accepted when only one key has that name
 */
internal fun Args.prefabKeyIn(keys: IdoCommandContext.() -> Collection<PrefabKey>) = prefabKey()
    .suggests {
        val typed = argument.lowercase()
        val sorted = keys().sortedBy { it.full }
        val (byName, rest) = sorted.partition { it.key.startsWith(typed) }
        suggest((byName + rest.filter { it.full.startsWith(typed) }).map { it.full })
    }
    .map { typed ->
        val all = keys()
        // A key without a namespace is read as minecraft:<name>
        typed.takeIf { it in all } ?: all.singleOrNull { it.key == typed.key }.takeIf { typed.namespace == "minecraft" }
            ?: fail("'$typed' is not a valid option")
    }

internal fun CommandSender.reportReload(kind: String, count: Int, errors: Int) {
    if (errors == 0) success("Reloaded $count $kind")
    else warn("Reloaded $count $kind with $errors errors, see console")
}
