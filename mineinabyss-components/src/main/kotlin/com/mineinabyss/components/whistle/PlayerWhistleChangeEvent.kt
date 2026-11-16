package com.mineinabyss.components.whistle

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class PlayerWhistleChangeEvent(
    player: Player,
    val old: Whistle?,
    val new: Whistle,
) : PlayerEvent(player) {
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
