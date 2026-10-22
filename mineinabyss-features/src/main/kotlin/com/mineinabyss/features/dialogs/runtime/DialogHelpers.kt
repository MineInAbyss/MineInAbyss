package com.mineinabyss.features.dialogs.runtime

import com.mineinabyss.components.unlocks.PlayerUnlocks
import com.mineinabyss.features.helpers.nextTick
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.getOrSetPersisting
import org.bukkit.Bukkit
import org.bukkit.entity.Player

const val SESSION_KEY = "session"
const val SPEAKER_KEY = "speaker"
const val STYLE_KEY = "dialog_style"

/** The npc a dialog opened from these actions keeps the player facing, set by `target_npc` */
const val FOCUS_KEY = "focus"

val ActionGroupContext.session: DialogSession? get() = environment[SESSION_KEY] as? DialogSession

// LuxDialogues runs answer callbacks off the main thread
internal fun onMainThread(block: () -> Unit) {
    if (Bukkit.isPrimaryThread()) block() else nextTick(block)
}

val Player.unlocks: Set<String> get() = toGeary().get<PlayerUnlocks>()?.keys.orEmpty()

fun Player.unlock(key: String) {
    toGeary().getOrSetPersisting<PlayerUnlocks> { PlayerUnlocks() }.keys += key
}

fun Player.lock(key: String) {
    toGeary().get<PlayerUnlocks>()?.keys?.remove(key)
}

fun Player.clearUnlocks() {
    toGeary().get<PlayerUnlocks>()?.keys?.clear()
}
