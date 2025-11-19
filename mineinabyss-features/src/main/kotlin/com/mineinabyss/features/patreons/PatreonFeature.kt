package com.mineinabyss.features.patreons

import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.playerDataOrNull
import com.mineinabyss.components.players.Patreon
import com.mineinabyss.features.AbyssContext
import com.mineinabyss.features.helpers.luckPerms
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.oneOf
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import github.scarsz.discordsrv.DiscordSRV
import kotlinx.serialization.Serializable
import net.luckperms.api.context.ImmutableContextSet
import net.luckperms.api.node.types.PrefixNode
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.koin.core.module.dsl.scopedOf
import java.time.Month
import java.util.*

@Serializable
class PatreonConfig(val enabled: Boolean = false, val patreonRoles: Map<String, PatreonRoles> = emptyMap()) {
    @Serializable
    data class PatreonRoles(val roleId: String, val patreonTier: Int)
}

val PatreonFeature = feature("patreon") {
    dependsOn {
        plugins("LuckPerms", "DiscordSRV")
    }

    val prefixContexts = listOf("global", "orth", "layerone", "layertwo", "layerthree", "layerfour", "layerfive")

    scopedModule {
        scoped<PatreonConfig> { get<AbyssContext>().config.patreon }
        scopedOf(::PatreonListener)
    }

    onEnable {
        listeners(get<PatreonListener>())
        DiscordSRV.api.subscribe(get<PatreonListener>())
    }

    onDisable {
        DiscordSRV.api.unsubscribe(get<PatreonListener>())
    }

    mainCommand {
        "patreon" {
            description = "Patreon-supporter related commands"
            "token" {
                description = "Redeem token"
                executes.asPlayer {
                    val player = sender as Player
                    if ((player.toGeary().get<Patreon>()?.tier ?: 0) == 0)
                        return@asPlayer player.error("This command is only for Patreon supporters!")

                    val patreon = player.toGeary().get<Patreon>() ?: return@asPlayer
                    val month = Month.of(Calendar.getInstance().get(Calendar.MONTH) + 1)

                    if (patreon.kitUsedMonth == month) {
                        player.error("You can only redeem this once a month.")
                        return@asPlayer
                    }

                    player.editPlayerData { mittyTokensHeld += patreon.tier }
                    patreon.kitUsedMonth = month
                    player.success("You redeemed a token for this month!")
                }
            }
            "prefix" {
                description = "Change your prefix emote"
                "remove" {
                    executes.asPlayer {
                        val player = sender as Player
                        val console = Bukkit.getServer().consoleSender
                        Bukkit.dispatchCommand(console, "luckperms user ${player.name} meta clear prefix")
                        player.success("Removed prefix")
                    }
                }
                "set" {
                    executes.asPlayer().args(
                        "emote" to Args.string(),
                        "location" to Args.string().oneOf(prefixContexts).default { "global" }
                    ) { emote, loc ->
                        val player = sender as Player

                        if (loc == "global") {
                            listOf(
                                PrefixNode.builder(":layer_orth::space_2::$emote::space_2:<#feac3b>", 10)
                                    .context(ImmutableContextSet.of("worldguard:region", "orth")).build(),
                                PrefixNode.builder(":layer_1::space_2::$emote::space_2:<#c83038>", 11)
                                    .context(ImmutableContextSet.of("worldguard:region", "layerone")).build(),
                                PrefixNode.builder(":layer_2::space_2::$emote::space_2:<#4c92ac>", 12)
                                    .context(ImmutableContextSet.of("worldguard:region", "layertwo")).build(),
                                PrefixNode.builder(":layer_3::space_2::$emote::space_2:<#4c92ac>", 13)
                                    .context(ImmutableContextSet.of("worldguard:region", "layerthree")).build(),
                                PrefixNode.builder(":layer_4::space_2::$emote::space_2:<#567252>", 14)
                                    .context(ImmutableContextSet.of("worldguard:region", "layerfour")).build(),
                                PrefixNode.builder(":layer_5::space_2::$emote::space_2:<#434868>", 15)
                                    .context(ImmutableContextSet.of("worldguard:region", "layerfive")).build(),
                            ).forEach { node ->
                                luckPerms.userManager.getUser(player.uniqueId)?.data()?.let { nodeMap ->
                                    nodeMap.toCollection().find { it.key == node.key }
                                        ?.let { node -> nodeMap.remove(node) }
                                    nodeMap.add(node)
                                }
                            }

                        } else {
                            val c: PrefixNode = when (loc) {
                                "orth" ->
                                    PrefixNode.builder(":layer_orth::space_2::$emote::space_2:<#feac3b>", 10)
                                        .context(ImmutableContextSet.of("worldguard:region", "orth")).build()

                                "layerone" ->
                                    PrefixNode.builder(":layer_1::space_2::$emote::space_2:<#c83038>", 11)
                                        .context(ImmutableContextSet.of("worldguard:region", "layerone")).build()

                                "layertwo" ->
                                    PrefixNode.builder(":layer_2::space_2::$emote::space_2:<#4c92ac>", 12)
                                        .context(ImmutableContextSet.of("worldguard:region", "layertwo")).build()

                                "layerthree" ->
                                    PrefixNode.builder(":layer_3::space_2::$emote::space_2:<#4c92ac>", 13)
                                        .context(ImmutableContextSet.of("worldguard:region", "layerthree")).build()

                                "layerfour" ->
                                    PrefixNode.builder(":layer_4::space_2::$emote::space_2:<#567252>", 14)
                                        .context(ImmutableContextSet.of("worldguard:region", "layerfour")).build()

                                "layerfive" ->
                                    PrefixNode.builder(":layer_5::space_2::$emote::space_2:<#434868>", 15)
                                        .context(ImmutableContextSet.of("worldguard:region", "layerfive")).build()

                                else -> PrefixNode.builder().build()
                            }
                            luckPerms.userManager.getUser(player.uniqueId)?.data()?.let { nodeMap ->
                                nodeMap.toCollection().find { it.key == c.key }
                                    ?.let { node -> nodeMap.remove(node) }
                                nodeMap.add(c)
                            }
                        }
                        luckPerms.userManager.saveUser(luckPerms.userManager.getUser(player.uniqueId)!!)
                        sender.success("Set prefix to $emote")
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
                        sender.success("${player.name}: ${player.toGeary().get<Patreon>()}")
                    }
                }
                "set_patreon" {
                    executes.asPlayer().args("tier" to Args.integer(min = 0)) { tier ->
                        with(player.toGeary()) {
                            val patreon = (get<Patreon>() ?: Patreon()).copy(tier = tier)
                            setPersisting(patreon)
                            sender.success("Set Patreon-component on ${player.name}: $patreon")
                        }
                    }
                }
            }
        }
    }
}
