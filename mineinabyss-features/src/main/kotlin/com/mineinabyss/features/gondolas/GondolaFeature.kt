package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.features.abyss
import com.mineinabyss.features.gondolas.pass.TicketConfig
import com.mineinabyss.features.gondolas.pass.unlockRoute
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.getOrSetPersisting
import com.mineinabyss.guiy.canvas.guiy
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.playerExecutes
import com.mineinabyss.idofront.commands.brigadier.suggests
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.messaging.error
import org.koin.core.module.dsl.scopedOf

/*
 * Gondolas system:
 * - Each player have an "UnlockedGondolas" component that stores the gondolas they have unlocked as strings. -> player.toGeary().get<UnlockedGondolas>()
 * - The server has a "ExistingGondolas" list that stores all the gondolas, it gets them from a config file. -> GondolasConfig.gondolas
 * - The server has a "LoadedGondolas" object stores all the active gondolas, that is, the ones players are able to use. -> LoadedGondolas.loaded
 * - Players are able to use (teleport) gondolas if they have them unlocked, and they are active (loaded).
 */
val GondolaFeature = feature("gondolas") {
    scopedModule {
        scoped<GondolasConfig> { config("gondolas", abyss.dataPath, GondolasConfig()).getOrLoad() }
        scoped<TicketConfig> { config("tickets", abyss.dataPath, TicketConfig()).getOrLoad() }
        scopedOf(::GondolasListener)
    }

    onEnable {
        gearyPaper.run {
            //LoadedGondolas
            //createGondolaTracker()
            val gondolasListener = get<GondolasListener>()
            listeners(gondolasListener)
            gondolasListener.startCooldownDisplayTask(plugin)
        }
    }
    mainCommand {
        "gondola" {
            description = "Commands for gondolas"
            permission = "mineinabyss.gondola"
            "list" {
                description = "Opens the gondola menu"
                permission = "mineinabyss.gondola.list"
                playerExecutes {
                    guiy(player) { GondolaSelectionMenu(player) }
                }
            }
            "unlock" {
                description = "Unlocks a route for a player"
                permission = "mineinabyss.gondola.unlock"
                playerExecutes(
                    Args.string().suggests {
                        suggestFiltering(get<TicketConfig>().tickets.keys.toList())
                    }
                ) { passID ->
//                        val gondolas = player.toGeary().get<UnlockedGondolas>()
//                            ?: return@playerExecutes
//                        gondolas.keys.add(gondola)
//                        player.success("Unlocked $gondola")
                    val ticket = get<TicketConfig>().tickets[passID] ?: return@playerExecutes player.error("Ticket $passID not found")
                    player.unlockRoute(ticket)
                }
            }
            "clear" {
                description = "Removes all associated gondolas from a player"
                permission = "mineinabyss.gondola.clear"
                playerExecutes {
                    val gondolas = player.toGeary().getOrSetPersisting<UnlockedGondolas> { UnlockedGondolas() }
                    gondolas.keys.clear()
                    player.error("Cleared all gondolas")
                }
            }
        }
    }
}
