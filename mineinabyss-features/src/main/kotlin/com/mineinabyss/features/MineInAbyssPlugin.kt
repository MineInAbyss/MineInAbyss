package com.mineinabyss.features

import com.charleskorn.kaml.AnchorsAndAliases
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.components.curse.AscensionEffect
import com.mineinabyss.dependencies.*
import com.mineinabyss.features.ansible.ConfigPullFeature
import com.mineinabyss.features.anticheese.AntiCheeseFeature
import com.mineinabyss.features.core.CoreFeature
import com.mineinabyss.features.cosmetics.CosmeticsFeature
import com.mineinabyss.features.curse.CurseFeature
import com.mineinabyss.features.custom_hud.CustomHudFeature
import com.mineinabyss.features.descent.DescentFeature
import com.mineinabyss.features.displayLocker.DisplayLockerFeature
import com.mineinabyss.features.gondolas.GondolaFeature
import com.mineinabyss.features.guilds.GuildFeature
import com.mineinabyss.features.guilds.database.GuildJoinQueue
import com.mineinabyss.features.guilds.database.GuildMessageQueue
import com.mineinabyss.features.guilds.database.Guilds
import com.mineinabyss.features.guilds.database.Players
import com.mineinabyss.features.helpers.Placeholders
import com.mineinabyss.features.hubstorage.HubStorageFeature
import com.mineinabyss.features.keepinventory.KeepInvFeature
import com.mineinabyss.features.layers.LayersFeature
import com.mineinabyss.features.lootcrates.LootCratesFeature
import com.mineinabyss.features.lootcrates.database.LootedChests
import com.mineinabyss.features.misc.MiscFeature
import com.mineinabyss.features.music.MusicFeature
import com.mineinabyss.features.npc.shopkeeping.ShopKeepingFeature
import com.mineinabyss.features.okibotravel.OkiboTravelFeature
import com.mineinabyss.features.orthbanking.OrthBankingFeature
import com.mineinabyss.features.patreons.PatreonFeature
import com.mineinabyss.features.playerprofile.PlayerProfileFeature
import com.mineinabyss.features.pvp.PvpFeature
import com.mineinabyss.features.quests.QuestFeature
import com.mineinabyss.features.relics.RelicsFeature
import com.mineinabyss.features.tools.ToolsFeature
import com.mineinabyss.features.tutorial.TutorialFeature
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.papermc.datastore.PrefabNamespaceMigrations
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.idofront.config.SingleConfig
import com.mineinabyss.idofront.config.WriteMode
import com.mineinabyss.idofront.features.MainCommand
import com.mineinabyss.idofront.features.MainCommandFeature
import com.mineinabyss.idofront.features.singleConfig
import com.mineinabyss.idofront.features.singlePluginLogger
import com.mineinabyss.idofront.messaging.ComponentLogger
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class MineInAbyssPlugin : JavaPlugin(), AbyssContext {
    override val di = DI.Companion {
        single<Plugin> { this@MineInAbyssPlugin }
        singlePluginLogger(this@MineInAbyssPlugin)
        singleConfig<AbyssFeatureConfig>("config.yml") {
            writeBack = WriteMode.ALWAYS
            format = Yaml(
                serializersModule = gearyPaper.worldManager.global.getAddon(SerializableComponents).formats.module,
                configuration = YamlConfiguration(
                    anchorsAndAliases = AnchorsAndAliases.Permitted(),
                    strictMode = false
                ),
            )
        }
        single {
            MainCommand(
                names = listOf("mineinabyss", "mia"),
                description = "The main command for Mine in Abyss",
                reloadCommandName = "reload",
                onBeforeReload = {
                    get<SingleConfig<AbyssFeatureConfig>>().updateCached()
                }
            )
        }
        // Exposed database connection
        // TODO migrate to our own DB library
        single {
            Database.connect(
                "jdbc:sqlite:" + this@MineInAbyssPlugin.dataFolder.path + "/data.db",
                "org.sqlite.JDBC"
            )
        }
    }

    override val logger: ComponentLogger by getLazy()
    override val config: AbyssFeatureConfig by getLazy()
    override val db: Database by getLazy()

    override fun onLoad() {
        AbyssContext.instance = this
        PrefabNamespaceMigrations.migrations += listOf("looty" to "mineinabyss", "mobzy" to "mineinabyss")
        transaction(db) {
            //addLogger(StdOutSqlLogger)
            SchemaUtils.createMissingTablesAndColumns(Guilds, Players, GuildJoinQueue, GuildMessageQueue, LootedChests)
        }

        if (isPlaceholderApiLoaded) Placeholders().register()
    }

    override fun onEnable() {
        gearyPaper.configure {
            world.autoscan {
                scan(
                    this@MineInAbyssPlugin.classLoader,
                    listOf(
                        "com.mineinabyss.features",
                        "com.mineinabyss.components",
                        "com.mineinabyss.mineinabyss.core"
                    )
                ) {
                    components()
                    subClassesOf<AscensionEffect>()
                }
            }
        }
        di.scope.loadAllCatching(
            AntiCheeseFeature,
            ConfigPullFeature,
            CoreFeature,
            CosmeticsFeature,
            CurseFeature,
            CustomHudFeature,
            DescentFeature,
            DisplayLockerFeature,
            GondolaFeature,
            GuildFeature,
            HubStorageFeature,
            KeepInvFeature,
            LayersFeature,
            LootCratesFeature,
            MiscFeature,
            MusicFeature,
            ShopKeepingFeature,
            OkiboTravelFeature,
            OrthBankingFeature,
            PatreonFeature,
            PlayerProfileFeature,
            PvpFeature,
            QuestFeature,
            RelicsFeature,
            ToolsFeature,
            TutorialFeature,
        )
        di.scope.load(MainCommandFeature)
    }

    override fun onDisable() {
        di.scope.close()
    }
}