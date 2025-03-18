package com.mineinabyss.features.patreons

import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.playerDataOrNull
import com.mineinabyss.components.players.Patreon
import com.mineinabyss.features.helpers.luckPerms
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import github.scarsz.discordsrv.DiscordSRV
import kotlinx.serialization.Serializable
import net.luckperms.api.context.ImmutableContextSet
import net.luckperms.api.node.types.PrefixNode
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.Month
import java.util.*

class PatreonFeature(val config: Config) : Feature() {
    override val dependsOn = setOf("LuckPerms", "DiscordSRV")

    val prefixContexts = listOf("global", "orth", "layerone", "layertwo", "layerthree", "layerfour", "layerfive")
    private val listener = PatreonListener(config)

    @Serializable
    class Config(val enabled: Boolean = false, val patreonRoles: Map<String, PatreonRoles> = emptyMap())

    @Serializable
    data class PatreonRoles(val roleId: String, val patreonTier: Int)

    override fun FeatureDSL.disable() {
        DiscordSRV.api.unsubscribe(listener)
    }

    override fun FeatureDSL.enable() {
        plugin.listeners(listener)
        DiscordSRV.api.subscribe(listener)

        mainCommand {
            "patreon"(desc = "Patreon-supporter related commands") {
                ("token")(desc = "Redeem token") {
                    playerAction {
                        val player = sender as Player
                        if ((player.toGeary().get<Patreon>()?.tier ?: 0) == 0)
                            return@playerAction player.error("This command is only for Patreon supporters!")

                        val patreon = player.toGeary().get<Patreon>() ?: return@playerAction
                        val month = Month.of(Calendar.getInstance().get(Calendar.MONTH) + 1)

                        if (patreon.kitUsedMonth == month) {
                            player.error("You can only redeem this once a month.")
                            return@playerAction
                        }

                        player.editPlayerData { mittyTokensHeld += patreon.tier }
                        patreon.kitUsedMonth = month
                    }
                }
                "prefix"(desc = "Change your prefix emote") {
                    "remove" {
                        playerAction {
                            val player = sender as Player
                            val console = Bukkit.getServer().consoleSender
                            Bukkit.dispatchCommand(console, "luckperms user ${player.name} meta clear prefix")
                            player.success("Removed prefix")
                        }
                    }
                    "set" {
                        val emote by stringArg()
                        val loc by optionArg(prefixContexts) { default = "global" }
                        playerAction {
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
                        val amount by intArg()
                        playerAction {
                            player.editPlayerData { mittyTokensHeld += amount }
                            sender.success("Gave $amount tokens to ${player.name}")
                        }
                    }
                    "check_token" {
                        playerAction {
                            sender.success("${player.name} has ${player.playerDataOrNull?.mittyTokensHeld} tokens")
                        }
                    }
                    "check_patreon" {
                        playerAction {
                            sender.success("${player.name}: ${player.toGeary().get<Patreon>()}")
                        }
                    }
                    "set_patreon" {
                        val tier by intArg()
                        playerAction {
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
        tabCompletion {
            when (args.size) {
                1 -> listOf("patreon").filter { it.startsWith(args[0]) }
                2 -> when (args[0]) {
                    "patreon" -> listOf("prefix", "token").filter { it.startsWith(args[1]) }
                    else -> null
                }

                3 -> when (args[1]) {
                    "prefix" -> listOf("remove", "set").filter { it.startsWith(args[2]) }
                    else -> null
                }

                4 -> when (args[2]) {
                    "set" -> listOf("kekw", "pogo", "pogyou", "pog").filter { it.startsWith(args[3]) }
                    else -> null
                }

                5 -> when (args[2]) {
                    "set" -> prefixContexts.filter { it.startsWith(args[4]) }
                    else -> null
                }

                else -> null
            }
        }
    }
}
