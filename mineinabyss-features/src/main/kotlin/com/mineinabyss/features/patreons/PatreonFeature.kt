package com.mineinabyss.features.patreons

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.components.PlayerData
import com.mineinabyss.components.PreResetPatreon
import com.mineinabyss.components.playerData
import com.mineinabyss.components.players.Patreon
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.luckPerms
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.entities.toOfflinePlayer
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.nms.nbt.editOfflinePDC
import com.mineinabyss.idofront.nms.nbt.getOfflinePDC
import com.mineinabyss.idofront.plugin.listeners
import github.scarsz.discordsrv.DiscordSRV
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.luckperms.api.context.ImmutableContextSet
import net.luckperms.api.node.types.PrefixNode
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.Month
import java.util.*
import kotlin.io.path.readLines
import kotlin.io.path.writeLines

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
                ("token" / "kit")(desc = "Redeem kit") {
                    playerAction {
                        val player = sender as Player
                        if (player.toGeary().get<Patreon>()?.tier == 0)
                            return@playerAction player.error("This command is only for Patreon supporters!")

                        val patreon = player.toGeary().get<Patreon>() ?: return@playerAction
                        val month = Month.of(Calendar.getInstance().get(Calendar.MONTH) + 1)

                        if (patreon.kitUsedMonth == month) {
                            player.error("You can only redeem this once a month.")
                            return@playerAction
                        }

                        player.playerData.mittyTokensHeld += patreon.tier
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
                                    PrefixNode.builder(":space_6:<#feac3b>Orth:space_2::$emote::space_2:>", 10)
                                        .context(ImmutableContextSet.of("worldguard:region", "orth")).build(),
                                    PrefixNode.builder(":space_6:<#c83038>Edge:space_2::$emote::space_2:>", 11)
                                        .context(ImmutableContextSet.of("worldguard:region", "layerone")).build(),
                                    PrefixNode.builder(":space_6:<#4c92ac>Forest:space_2::$emote::space_2:>", 12)
                                        .context(ImmutableContextSet.of("worldguard:region", "layertwo")).build(),
                                    PrefixNode.builder(":space_6:<#852d66>Fault:space_2::$emote::space_2:>", 13)
                                        .context(ImmutableContextSet.of("worldguard:region", "layerthree")).build(),
                                    PrefixNode.builder(":space_6:<#852d66>Goblets:space_2::$emote::space_2:>", 14)
                                        .context(ImmutableContextSet.of("worldguard:region", "layerfour")).build(),
                                    PrefixNode.builder(":space_6:<#434868>Sea:space_2::$emote::space_2:>", 15)
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
                                    "orth" -> PrefixNode.builder(
                                        ":space_6:<#feac3b>Orth:space_2::$emote::space_2:>",
                                        10
                                    )
                                        .context(ImmutableContextSet.of("worldguard:region", "orth")).build()

                                    "layerone" -> PrefixNode.builder(
                                        ":space_6:<#c83038>Edge:space_2::$emote::space_2:>",
                                        11
                                    )
                                        .context(ImmutableContextSet.of("worldguard:region", "layerone")).build()

                                    "layertwo" -> PrefixNode.builder(
                                        ":space_6:<#4c92ac>Forest:space_2::$emote::space_2:>",
                                        12
                                    )
                                        .context(ImmutableContextSet.of("worldguard:region", "layertwo")).build()

                                    "layerthree" -> PrefixNode.builder(
                                        ":space_6:<#852d66>Fault:space_2::$emote::space_2:>",
                                        13
                                    )
                                        .context(ImmutableContextSet.of("worldguard:region", "layerthree")).build()

                                    "layerfour" -> PrefixNode.builder(
                                        ":space_6:<#852d66>Goblets:space_2::$emote::space_2:>",
                                        14
                                    )
                                        .context(ImmutableContextSet.of("worldguard:region", "layerfour")).build()

                                    "layerfive" -> PrefixNode.builder(
                                        ":space_6:<#434868>Sea:space_2::$emote::space_2:>",
                                        15
                                    )
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
                    val json = Json {
                        this.ignoreUnknownKeys = true
                        this.isLenient = true
                    }
                    "fetch_tokens" {
                        action {
                            abyss.plugin.launch(abyss.plugin.asyncDispatcher) {
                                val preResetDatas = abyss.plugin.server.offlinePlayers.map { offlinePlayer ->
                                    runCatching {
                                        val offlinePdc = offlinePlayer.getOfflinePDC() ?: return@map null
                                        val heldTokens = (offlinePlayer.player?.playerData
                                            ?: offlinePdc.decode<PlayerData>())?.mittyTokensHeld ?: 0
                                        val wasPatreon = ((offlinePlayer.player?.toGeary()?.get<Patreon>()
                                            ?: offlinePdc.decode<Patreon>())?.tier ?: 0) > 0
                                        PreResetPatreon(
                                            offlinePlayer.name.toString(),
                                            offlinePlayer.uniqueId,
                                            heldTokens,
                                            wasPatreon
                                        )
                                    }.getOrNull()
                                }.filterNotNull().toSet()
                                    .sortedWith(compareByDescending(PreResetPatreon::wasActivePatreon)
                                        .thenByDescending { it.heldTokens })

                                abyss.dataPath
                                    .resolve("preResetPatreons.txt")
                                    .writeLines(preResetDatas.map {
                                        runCatching { json.encodeToString(it) }.getOrNull() ?: it.toString()
                                    })
                                abyss.logger.s("done!")
                            }
                        }
                    }
                    "reset_extra_token" {
                        action {
                            abyss.plugin.launch(abyss.plugin.asyncDispatcher) {
                                abyss.dataPath.resolve("preResetPatreons.txt").readLines().mapNotNull {
                                    runCatching { json.decodeFromString<PreResetPatreon>(it) }.getOrNull()
                                }.filter { it.wasActivePatreon && it.heldTokens > 0 }.forEach {
                                    it.uuid.toPlayer()?.let { player ->
                                        player.playerData.mittyTokensHeld = it.heldTokens + 1 //add one extra token
                                    } ?: it.uuid.toOfflinePlayer().editOfflinePDC {
                                        (decode<PlayerData>() ?: PlayerData()).mittyTokensHeld =
                                            it.heldTokens + 1 //add one extra token
                                    }
                                }
                            }
                        }
                    }
                    "give_token" {
                        val amount by intArg()
                        playerAction {
                            player.playerData.mittyTokensHeld += amount
                            sender.success("Gave $amount tokens to ${player.name}")
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
