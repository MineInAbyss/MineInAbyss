package com.mineinabyss.patreons

import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.geary
import com.mineinabyss.playerprofile.luckPerms
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.luckperms.api.context.ImmutableContextSet
import net.luckperms.api.node.types.PrefixNode
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@Serializable
@SerialName("patreon")
class PatreonFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {

        geary {
            systems()
        }
        registerEvents(PatreonListener())

        commands {
            mineinabyss {
                "patreon"(desc = "Patreon-supporter related commands") {
                    "prefix"(desc = "Change your prefix emote") {
                        "remove" {
                            playerAction {
                                val player = sender as Player
                                val console = Bukkit.getServer().consoleSender
                                Bukkit.dispatchCommand(console, "luckperms user ${player.name} meta clear prefix")
                            }
                        }
                        "set" {
                            val emote by stringArg()
                            val locs = listOf(
                                "global",
                                "orth",
                                "layerone",
                                "layertwo",
                                "layerthree",
                                "layerfour",
                                "layerfive",
                                "nazarick",
                                "camelot"
                            )
                            val loc by optionArg(locs) {
                                default = "global"
                                parseErrorMessage = { "No such enchantment: $passed. \nAvailable ones are: \n$locs" }
                            }
                            playerAction {
                                val player = sender as Player

                                if (loc == "global") {
                                    listOf(
                                        PrefixNode.builder("§eOrth :$emote: §e-", 10)
                                            .context(ImmutableContextSet.of("worldguard:region", "orth")).build(),
                                        PrefixNode.builder("§cEdge :$emote: §c-", 11)
                                            .context(ImmutableContextSet.of("worldguard:region", "layerone")).build(),
                                        PrefixNode.builder("§9Forest :$emote: §9-", 12)
                                            .context(ImmutableContextSet.of("worldguard:region", "layertwo")).build(),
                                        PrefixNode.builder("§dFault :$emote: §d-", 13)
                                            .context(ImmutableContextSet.of("worldguard:region", "layerthree")).build(),
                                        PrefixNode.builder("§5Goblets :$emote: §5-", 14)
                                            .context(ImmutableContextSet.of("worldguard:region", "layerfour")).build(),
                                        PrefixNode.builder("§8Sea :$emote: §8-", 15)
                                            .context(ImmutableContextSet.of("worldguard:region", "layerfive")).build(),
                                        PrefixNode.builder("§8Nazarick :$emote: §8-", 16)
                                            .context(ImmutableContextSet.of("world", "nazarick")).build(),
                                        PrefixNode.builder("§3Camelot :$emote: §3-", 17)
                                            .context(ImmutableContextSet.of("world", "camelot")).build()
                                    ).forEach { node ->
                                        luckPerms.userManager.getUser(player.uniqueId)?.data()?.add(node)
                                    }

                                } else {
                                    val c: PrefixNode = when (loc) {
                                        "orth" -> PrefixNode.builder("§eOrth :$emote: §e-", 10)
                                            .context(ImmutableContextSet.of("worldguard:region", "orth")).build()
                                        "layerone" -> PrefixNode.builder("§cEdge :$emote: §c-", 11)
                                            .context(ImmutableContextSet.of("worldguard:region", "layerone")).build()
                                        "layertwo" -> PrefixNode.builder("§9Forest :$emote: §9-", 12)
                                            .context(ImmutableContextSet.of("worldguard:region", "layertwo")).build()
                                        "layerthree" -> PrefixNode.builder("§dFault :$emote: §d-", 13)
                                            .context(ImmutableContextSet.of("worldguard:region", "layerthree")).build()
                                        "layerfour" -> PrefixNode.builder("§5Goblets :$emote: §5-", 14)
                                            .context(ImmutableContextSet.of("worldguard:region", "layerfour")).build()
                                        "layerfive" -> PrefixNode.builder("§8Sea :$emote: §8-", 15)
                                            .context(ImmutableContextSet.of("worldguard:region", "layerfive")).build()
                                        "nazarick" -> PrefixNode.builder("§8Nazarick :$emote: §8-", 16)
                                            .context(ImmutableContextSet.of("world", "nazarick")).build()
                                        "camelot" -> PrefixNode.builder("§3Camelot :$emote: §3-", 17)
                                            .context(ImmutableContextSet.of("world", "camelot")).build()
                                        else -> PrefixNode.builder().build()
                                    }
                                    luckPerms.userManager.getUser(player.uniqueId)?.data()?.add(c)
                                }
                                luckPerms.userManager.saveUser(luckPerms.userManager.getUser(player.uniqueId)!!)
                            }
                        }
                    }
                }
            }
            val locs =
                listOf(
                    "global",
                    "orth",
                    "layerone",
                    "layertwo",
                    "layerthree",
                    "layerfour",
                    "layerfive",
                    "nazarick",
                    "camelot",
                )
            tabCompletion {
                when (args.size) {
                    1 -> listOf(
                        "patreon"
                    ).filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "patreon" -> listOf("prefix")
                            else -> null
                        }
                    }
                    3 -> {
                        when (args[1]) {
                            "prefix" -> listOf("remove", "set")
                            else -> null
                        }
                    }
                    4 -> {
                        when (args[2]) {
                            "set" -> listOf("kekw", "pogo", "pogyou", "pog")
                            else -> null
                        }
                    }
                    5 -> {
                        when (args[2]) {
                            "set" -> locs
                            else -> null
                        }
                    }
                    else -> null
                }
            }
        }

    }
}
