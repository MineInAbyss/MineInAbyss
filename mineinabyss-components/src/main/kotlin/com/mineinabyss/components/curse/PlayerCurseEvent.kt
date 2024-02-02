package com.mineinabyss.components.curse

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class PlayerCurseEvent(player: Player, val effects: List<AscensionEffect>) : PlayerEvent(player), Cancellable {

    override fun getHandlers(): HandlerList = handlerList
    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }

    private var cancelled: Boolean = false
    override fun isCancelled() = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }
}
