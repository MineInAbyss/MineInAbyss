package com.mineinabyss.features.advancements

import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.di.Features.advancements
import com.mineinabyss.idofront.commands.arguments.playerArg
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Configurable
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureWithContext
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.logSuccess
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import eu.endercentral.crazy_advancements.CrazyAdvancementsAPI
import eu.endercentral.crazy_advancements.NameKey
import eu.endercentral.crazy_advancements.manager.AdvancementManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class AdvancementsFeature : FeatureWithContext<AdvancementsFeature.Context>(::Context) {

    class Context : Configurable<AdvancementConfig> {
        override val configManager = config("advancements", abyss.plugin.dataFolder.toPath(), AdvancementConfig(), onReload = {
            advancements.advancementManager.updateAdvancement()
            logSuccess("Advancements reloaded")
        })
        val advancementsListener = AdvancementsListener()
        val advancementManager: AdvancementManager get() = AdvancementManager.getAccessibleManager(ADVANCEMENT_NAMEKEY) ?: run { AdvancementManager(ADVANCEMENT_NAMEKEY).makeAccessible(); advancementManager }
    }
    override val dependsOn: Set<String> get() = setOf("CrazyAdvancementsAPI")
    override fun FeatureDSL.enable() {
        createAdvancements()
        plugin.listeners(context.advancementsListener)

        mainCommand {
                "advancements" {
                    "list" {
                        action {
                            sender.success(context.config.advancements.keys.joinToString())
                        }
                    }
                    "grant" {
                        val player: Player by playerArg()
                        val nameKey: String by stringArg()
                        playerAction {
                            when (player.grantAdvancement(nameKey)) {
                                true -> sender.success("Granted advancement $nameKey to ${player.name}")
                                false -> sender.error("Player ${player.name} already had advancement $nameKey")
                            }
                        }
                    }
                    "revoke" {
                        val player: Player by playerArg()
                        val nameKey: String by stringArg()
                        playerAction {
                            when (player.revokeAdvancement(nameKey)) {
                                true -> sender.success("Revoked advancement $nameKey from ${player.name}")
                                false -> sender.error("Player ${player.name} did not have advancement $nameKey")
                            }
                        }
                    }
                    "tab" {
                        val player: Player by playerArg()
                        "get" {
                            action {
                                sender.success(CrazyAdvancementsAPI.getActiveTab(player)?.key?.toString() ?: "null")
                            }
                        }
                        "set" {
                            val nameKey: String by stringArg()
                            action {
                                CrazyAdvancementsAPI.setActiveTab(player, NameKey(nameKey))
                                sender.success("Set ${player.name}'s tab to $nameKey")
                            }
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("advancements").filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "advancements" -> listOf("tab", "grant", "revoke")
                            else -> listOf()
                        }.filter { it.startsWith(args[1]) }
                    }

                    3 -> {
                        when (args[1]) {
                            "tab" -> listOf("get", "set")
                            "grant", "revoke" -> Bukkit.getOnlinePlayers().map { it.name }
                            else -> listOf()
                        }.filter { it.startsWith(args[2]) }
                    }
                    4 -> {
                        when(args[2]) {
                            "set", "get" -> Bukkit.getOnlinePlayers().map { it.name }
                            "grant", "revoke" -> context.config.advancements.map { it.key + it.value.mapChildren().keys }.toSet()
                            else -> listOf()
                        }.filter { it.startsWith(args[3]) }
                    }

                    5 -> {
                        when(args[2]) {
                            "set" -> context.config.advancements.map { it.key + it.value.mapChildren().keys }.toSet()
                            else -> listOf()
                        }.filter { it.startsWith(args[4]) }
                    }

                    else -> listOf()
                }
        }
    }
}
