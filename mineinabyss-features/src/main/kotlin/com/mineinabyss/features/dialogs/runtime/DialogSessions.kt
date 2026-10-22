package com.mineinabyss.features.dialogs.runtime

import com.mineinabyss.features.abyss
import com.mineinabyss.features.dialogs.DialogRegistry
import com.mineinabyss.features.dialogs.DialogsConfig
import com.mineinabyss.features.dialogs.model.DialogNode
import com.mineinabyss.features.helpers.loading.gearyYaml
import com.mineinabyss.geary.prefabs.PrefabKey
import org.aselstudios.luxdialoguesapi.Events.DialogueStopEvent
import org.aselstudios.luxdialoguesapi.LuxDialoguesAPI
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class DialogSessions(
    private val root: Path,
    internal val config: DialogsConfig,
) : Listener {
    private val sessions = ConcurrentHashMap<UUID, DialogSession>()
    private val yaml = gearyYaml()

    var registry: DialogRegistry = DialogRegistry.empty(yaml)
        private set

    val loaded = CompletableFuture<Unit>()

    fun reload(): DialogRegistry = try {
        registry = DialogRegistry.load(root, yaml)
        registry.danglingReferences().forEach { abyss.logger.w("Dialog reference to missing dialog: $it") }
        registry
    } finally {
        loaded.complete(Unit)
    }

    private fun current(player: Player): DialogSession? = sessions[player.uniqueId]

    /** True for any open LuxDialogues box, including ones not started through this feature */
    fun isInDialog(player: Player): Boolean = LuxDialoguesAPI.getProvider().isInDialogue(player) == true

    fun start(player: Player, key: PrefabKey): Boolean {
        val node = registry[key] ?: run {
            abyss.logger.w("Tried to open missing dialog $key for ${player.name}")
            return false
        }
        start(player, node, key.full)
        return true
    }

    fun start(
        player: Player,
        node: DialogNode,
        id: String,
        speaker: String? = null,
        style: String? = null,
        environment: Map<String, Any?> = emptyMap(),
    ) {
        current(player)?.close(clearBox = false)
        DialogSession(player, id, this, speaker ?: " ", style, environment).show(node)
    }

    fun close(player: Player) {
        current(player)?.close()
    }

    fun closeAll() = sessions.values.toList().forEach { it.close() }

    internal fun register(session: DialogSession) {
        sessions[session.player.uniqueId] = session
    }

    internal fun unregister(session: DialogSession) {
        sessions.remove(session.player.uniqueId, session)
    }

    // A new session builds its first segment before it registers, so an idle player still belongs to it
    internal fun isCurrentOrIdle(session: DialogSession): Boolean =
        sessions[session.player.uniqueId].let { it == null || it === session }

    @EventHandler
    fun DialogueStopEvent.onStop() {
        current(player)?.takeIf { it.dialogue === dialogue }?.close(clearBox = false)
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        current(player)?.close(clearBox = false)
    }
}
