package com.mineinabyss.features.gondolas

import com.bergerkiller.reflection.org.bukkit.BSimplePluginManager.plugins
import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.features.abyss
import com.mineinabyss.features.gondolas.pass.TicketConfig
import com.mineinabyss.features.gondolas.pass.unlockRoute
import com.mineinabyss.features.okibotravel.OkiboTravelFeature
import com.mineinabyss.features.okibotravel.OkiboTravelFeature.Context
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.getOrSetPersisting
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Configurable
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureWithContext
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import org.bukkit.entity.Player

/*
 * Gondolas system:
 * - Each player have an "UnlockedGondolas" component that stores the gondolas they have unlocked as strings. -> player.toGeary().get<UnlockedGondolas>()
 * - The server has a "ExistingGondolas" list that stores all the gondolas, it gets them from a config file. -> GondolasConfig.gondolas
 * - The server has a "LoadedGondolas" object stores all the active gondolas, that is, the ones players are able to use. -> LoadedGondolas.loaded
 * - Players are able to use (teleport) gondolas if they have them unlocked, and they are active (loaded).
 */
class GondolaFeature : FeatureWithContext<GondolaFeature.Context>(::Context) {

    class Context : Configurable<GondolasConfig> {
        override val configManager =
            config("gondolas", abyss.dataPath, GondolasConfig())
        val ticketsCfg by config("tickets", abyss.dataPath, TicketConfig())
        val gondolasListener = GondolasListener()
    }

    override fun FeatureDSL.enable() = gearyPaper.run {
        //LoadedGondolas
        //createGondolaTracker()
        plugin.listeners(context.gondolasListener)
        mainCommand {
            "gondola"(desc = "Commands for gondolas") {
                permission = "mineinabyss.gondola"
                "list"(desc = "Opens the gondola menu") {
                    permission = "mineinabyss.gondola.list"
                    playerAction {
                        guiy { GondolaSelectionMenu(player) }
                    }
                }
                "unlock"(desc = "Unlocks a route for a player") {
                    permission = "mineinabyss.gondola.unlock"
                    val passID by stringArg()
                    playerAction {
//                        val gondolas = player.toGeary().get<UnlockedGondolas>()
//                            ?: return@playerAction
//                        gondolas.keys.add(gondola)
//                        player.success("Unlocked $gondola")
                        val ticket = context.ticketsCfg.tickets[passID]?: return@playerAction player.error("Ticket $passID not found")
                        player.unlockRoute(ticket)
                    }
                }
                "clear"(desc = "Removes all associated gondolas from a player") {
                    permission = "mineinabyss.gondola.clear"
                    playerAction {
                        val gondolas = player.toGeary().getOrSetPersisting<UnlockedGondolas> { UnlockedGondolas() }
                        gondolas.keys.clear()
                        player.error("Cleared all gondolas")
                    }
                }
            }
        }
        tabCompletion {
            when (args.size) {
                1 -> listOf("gondola").filter { it.startsWith(args[0], true) }
                2 -> if (args[0] == "gondola") listOf("list", "unlock", "clear").filter { it.startsWith(args[1], true) } else null
                3 -> if (args[0] == "gondola" && args[1] == "unlock") {
                    context.ticketsCfg.tickets.keys.filter { it.startsWith(args[2], true) }
                    //context.config.gondolas.keys.filter { it.startsWith(args[2], true) }
                } else null
                else -> null
            }
        }
    }
}
