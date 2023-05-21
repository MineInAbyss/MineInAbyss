package com.mineinabyss.mineinabyss.core

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.plugin.Services
import github.scarsz.discordsrv.DiscordSRV
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.jetbrains.exposed.sql.Database

/** A reference to the MineInAbyss plugin */
//val mineInAbyss: MineInAbyssPlugin by lazy { Bukkit.getPluginManager().getPlugin("MineInAbyss") as MineInAbyssPlugin }
val discordSRV: DiscordSRV by lazy { Bukkit.getPluginManager().getPlugin("DiscordSRV") as DiscordSRV }
val abyss by DI.observe<AbyssContext>()

class AbyssContext(
    val plugin: MineInAbyssPlugin,
    val worldManager: AbyssWorldManager
) {
    val isChattyLoaded get() = plugin.server.pluginManager.isPluginEnabled("chatty")
    val isGSitLoaded get() = plugin.server.pluginManager.isPluginEnabled("GSit")
    val isPlaceholderApiLoaded get() = plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")
    val isHMCCosmeticsEnabled get() = plugin.server.pluginManager.isPluginEnabled("HMCCosmetics")
    val isMCCosmeticsEnabled get() = plugin.server.pluginManager.isPluginEnabled("MCCosmetics")

    val econ: Economy? = Services.getOrNull<Economy>()

    val configController = config<AbyssConfig>("config") {
        formats {
            mapOf(
                "yml" to Yaml(
                    // We autoscan in our Feature classes so need to use Geary's module.
                    serializersModule = serializableComponents.serializers.module,
                    configuration = YamlConfiguration(
                        extensionDefinitionPrefix = "x-"
                    )
                )
            )
        }
        plugin.fromPluginPath(loadDefault = true)
    }
    val config: AbyssConfig by configController

    val miaSubcommands = mutableListOf<Command.() -> Unit>()
    val tabCompletions = mutableListOf<MineInAbyssPlugin.TabCompletion.() -> List<String>?>()
    val db = Database.connect("jdbc:sqlite:" + plugin.dataFolder.path + "/data.db", "org.sqlite.JDBC")


    val commandExecutor: IdofrontCommandExecutor = object : IdofrontCommandExecutor(), TabCompleter {
        override val commands = commands(plugin) {
            ("mineinabyss" / "mia")(desc = "The main command for Mine in Abyss") {
                miaSubcommands.forEach { it() }
            }
        }

        override fun onTabComplete(
            sender: CommandSender,
            command: org.bukkit.command.Command,
            alias: String,
            args: Array<String>
        ): List<String> {
            val tab = MineInAbyssPlugin.TabCompletion(sender, command, alias, args)
            return tabCompletions.mapNotNull { it(tab) }.flatten()
        }
    }
}
