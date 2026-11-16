package com.mineinabyss.features.whistle

import com.mineinabyss.components.whistle.PlayerWhistle
import com.mineinabyss.components.whistle.PlayerWhistleChangeEvent
import com.mineinabyss.components.whistle.Whistle
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.oneOf
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

val Player.whistle: Whistle? get() = toGeary().get<PlayerWhistle>()?.rank

fun Player.hasWhistle(atLeast: Whistle): Boolean = (whistle ?: return false) >= atLeast

fun Player.setWhistle(rank: Whistle) {
    val old = whistle
    if (old == rank) return
    toGeary().setPersisting(PlayerWhistle(rank))
    PlayerWhistleChangeEvent(this, old, rank).callEvent()
}

// Meant to only ever increment whistle never lower
fun Player.grantWhistle(rank: Whistle, silent: Boolean = false) {
    if (hasWhistle(rank)) return
    setWhistle(rank)
    if (!silent) success("You are now a ${rank.id} whistle")
}

class WhistleListener : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun PlayerJoinEvent.grantBell() {
        player.grantWhistle(Whistle.BELL, silent = true)
    }
}

val WhistleFeature = module("whistle") {
    require(get<AbyssFeatureConfig>().whistle.enabled) { "Whistle feature is disabled" }
    listeners(WhistleListener())
}.mainCommand {
    "whistle" {
        "set" {
            permission = "mineinabyss.whistle.set"
            executes.args(
                "player" to Args.offlinePlayer(),
                "rank" to Args.word().oneOf(Whistle.entries.map { it.id }),
            ) { target, rank ->
                val player = target.player ?: fail("${target.name} is not online")
                player.setWhistle(Whistle.valueOf(rank.uppercase()))
                sender.success("${player.name} is now a $rank whistle")
            }
        }
        "get" {
            executes.args("player" to Args.offlinePlayer()) { target ->
                val player = target.player ?: fail("${target.name} is not online")
                sender.info("${player.name} is a ${player.whistle?.id ?: "no"} whistle")
            }
            executes.asPlayer {
                sender.info("You are a ${player.whistle?.id ?: "no"} whistle")
            }
        }
    }
}
