package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.gondolas.pass.TicketConfig
import com.mineinabyss.features.gondolas.pass.unlockRoute
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.getOrSetPersisting
import com.mineinabyss.guiy.canvas.guiy
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.oneOf
import com.mineinabyss.idofront.di.DI.get
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.features.singleConfig
import com.mineinabyss.idofront.features.task
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success

/**
 * Gondolas system:
 * - Each player have an "UnlockedGondolas" component that stores the gondolas they have unlocked as strings. -> player.toGeary().get<UnlockedGondolas>()
 * - The server has a "ExistingGondolas" list that stores all the gondolas, it gets them from a config file. -> GondolasConfig.gondolas
 * - The server has a "LoadedGondolas" object stores all the active gondolas, that is, the ones players are able to use. -> LoadedGondolas.loaded
 * - Players are able to use (teleport) gondolas if they have them unlocked, and they are active (loaded).
 */
val GondolaFeature = module("gondola") {
    require(get<AbyssFeatureConfig>().gondolas.enabled) { "Gondolas feature is disabled" }

    // config
    val gondolaConfig by singleConfig<GondolasConfig>("gondolas.yml") { default = GondolasConfig() }
    val ticketConfig by singleConfig<TicketConfig>("tickets.yml") { default = TicketConfig() }

    // listeners
    val gondolasListener by single { new(::GondolasListener) }

    listeners(gondolasListener)
    task(gondolasListener.job)
}.mainCommand {
    "gondola" {
        description = "Commands for gondolas"

        "list" {
            description = "Opens the gondola menu"
            executes.asPlayer {
                guiy(player) { GondolaSelectionMenu(player) }
            }
        }

        "unlock" {
            description = "Unlocks a route for a player"
            permission = "mineinabyss.gondola.unlock"
            executes.asPlayer().args("keys" to Args.string().oneOf { get<TicketConfig>().tickets.keys.toList() })
            { passID ->
                val ticket = get<TicketConfig>().tickets[passID] ?: return@args player.error("Ticket $passID not found")
                player.unlockRoute(ticket)
                player.success("Successfully unlocked route $passID");
            }
        }
        "clear" {
            description = "Removes all associated gondolas from a player"
            permission = "mineinabyss.gondola.clear"
            executes.asPlayer {
                val gondolas = player.toGeary().getOrSetPersisting<UnlockedGondolas> { UnlockedGondolas() }
                gondolas.keys.clear()
                player.success("Your gondolas have been removed")
            }
        }
    }
}