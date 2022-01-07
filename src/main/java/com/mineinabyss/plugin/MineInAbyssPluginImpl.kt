package com.mineinabyss.plugin

import com.mineinabyss.geary.minecraft.dsl.GearyAddon
import com.mineinabyss.geary.minecraft.dsl.GearyLoadPhase
import com.mineinabyss.geary.minecraft.dsl.gearyAddon
import com.mineinabyss.geary.minecraft.store.PrefabNamespaceMigrations
import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.platforms.IdofrontPlatforms
import com.mineinabyss.idofront.plugin.getServiceOrNull
import com.mineinabyss.idofront.plugin.registerService
import com.mineinabyss.mineinabyss.core.*
import com.mineinabyss.mineinabyss.data.GuildJoinQueue
import com.mineinabyss.mineinabyss.data.Guilds
import com.mineinabyss.mineinabyss.data.MessageQueue
import com.mineinabyss.mineinabyss.data.Players
import net.milkbowl.vault.economy.Economy
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

class MineInAbyssPluginImpl : MineInAbyssPlugin() {
    override fun onLoad() {
        IdofrontPlatforms.load(this, "mineinabyss")
    }

    override fun onEnable() {
        saveDefaultConfig()

        gearyAddon {
            PrefabNamespaceMigrations.migrations += listOf("looty" to "mineinabyss", "mobzy" to "mineinabyss")

            registerService<AbyssContext>(object : AbyssContext {
                override val econ = getServiceOrNull<Economy>("Vault")
                override val addonScope: GearyAddon = this@gearyAddon
                override val miaSubcommands = mutableListOf<Command.() -> Unit>()
                override val tabCompletions = mutableListOf<TabCompletion.() -> List<String>?>()
                override val db = Database.connect("jdbc:sqlite:" + dataFolder.path + "/data.db", "org.sqlite.JDBC")

                override val commandExecutor = object : IdofrontCommandExecutor(), TabCompleter {
                    override val commands = commands(this@MineInAbyssPluginImpl) {
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
                        val tab = TabCompletion(sender, command, alias, args)
                        return tabCompletions.mapNotNull { it(tab) }.flatten()
                    }
                }
            })

            autoScanAll()
            autoScan<AbyssFeature>()

            startup {
                GearyLoadPhase.ENABLE {
                    val config = MIAConfigImpl()
                    config.load()
                    registerService<MIAConfig>(config)
                    registerService<AbyssWorldManager>(AbyssWorldManagerImpl())
                }
            }
        }

        transaction(AbyssContext.db) {
            addLogger(StdOutSqlLogger)

            SchemaUtils.createMissingTablesAndColumns(Guilds, Players, GuildJoinQueue, MessageQueue)
        }
    }
}
