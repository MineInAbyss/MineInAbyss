package com.mineinabyss.features.patreons

import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.playerDataOrNull
import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.luckPerms
import com.mineinabyss.features.helpers.nextTick
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.features.requirePlugins
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import kotlinx.serialization.Serializable
import net.luckperms.api.event.user.UserDataRecalculateEvent
import org.bukkit.Bukkit
import java.time.YearMonth

@Serializable
class PatreonConfig(
    val enabled: Boolean = false,
    /** LuckPerms group name to tier, groups are kept in sync with Discord roles by DiscordSRV group sync */
    val groupTiers: Map<String, Int> = mapOf("patreon" to 1, "patreonplus" to 2),
)

val PatreonFeature = module("patreon") {
    require(get<AbyssFeatureConfig>().patreon.enabled) { "Patreon feature is disabled" }
    requirePlugins("LuckPerms", "Chatty")

    listeners(PatreonListener())

    // Fires async and for every data change, including losing a Patreon group while online
    val recalculation = luckPerms.eventBus.subscribe(abyss, UserDataRecalculateEvent::class.java) { event ->
        val uuid = event.user.uniqueId
        nextTick { Bukkit.getPlayer(uuid)?.revokeLapsedPatreonPerks() }
    }
    addCloseable { recalculation.close() }
}.mainCommand {
    "patreon" {
        description = "Patreon-supporter related commands"
        "token" {
            description = "Redeem token"
            executes.asPlayer {
                val tier = player.patreonTier
                if (tier == 0) return@asPlayer player.error("This command is only for Patreon supporters!")

                val month = YearMonth.now().toString()
                if (player.patreonPerks.tokenRedeemedMonth == month)
                    return@asPlayer player.error("You can only redeem this once a month")

                player.editPlayerData { mittyTokensHeld += tier }
                player.editPatreonPerks { copy(tokenRedeemedMonth = month) }
                player.success("You redeemed a token for this month!")
            }
        }
        "prefix" {
            description = "Change your prefix emote"
            "remove" {
                executes.asPlayer {
                    player.editPatreonPerks { copy(emote = null) }
                    player.success("Removed prefix")
                }
            }
            "set" {
                executes.asPlayer().args("emote" to Args.word()) { emote ->
                    if (player.patreonTier == 0) return@args player.error("This command is only for Patreon supporters!")
                    player.editPatreonPerks { copy(emote = emote) }
                    player.success("Set prefix to $emote")
                }
            }
        }
        "admin" {
            "give_token" {
                executes.asPlayer().args("amount" to Args.integer(min = 0)) { amount ->
                    player.editPlayerData { mittyTokensHeld += amount }
                    sender.success("Gave $amount tokens to ${player.name}")
                }
            }
            "check_token" {
                executes.asPlayer {
                    sender.success("${player.name} has ${player.playerDataOrNull?.mittyTokensHeld} tokens")
                }
            }
            "check_patreon" {
                executes.asPlayer {
                    sender.success("${player.name}: tier ${player.patreonTier}, ${player.patreonPerks}")
                }
            }
        }
    }
}
