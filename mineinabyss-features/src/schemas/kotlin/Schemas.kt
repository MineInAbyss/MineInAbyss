import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.achievements.AchievementsConfig
import com.mineinabyss.features.anticheese.AntiCheeseConfig
import com.mineinabyss.features.dialogs.DialogsConfig
import com.mineinabyss.features.goals.GoalsConfig
import com.mineinabyss.features.guilds.GuildsConfig
import com.mineinabyss.features.layers.LayersConfig
import com.mineinabyss.features.lootcrates.LootCratesConfig
import com.mineinabyss.features.music.MusicConfig
import com.mineinabyss.features.okibotravel.OkiboTravelConfig
import com.mineinabyss.features.quests.QuestConfig
import com.mineinabyss.features.respawn.RespawnConfig
import com.mineinabyss.features.triggerpoints.TriggerPointsConfig
import com.mineinabyss.features.tutorial.Tutorial
import com.mineinabyss.geary.papermc.schemas.GearyPrefabSchemas.gearyPrefabs
import com.mineinabyss.jsonschema.writer.SchemaWriter
import com.mineinabyss.jsonschema.writer.writeSchemas
import kotlinx.serialization.KSerializer

fun main(args: Array<String>) = writeSchemas(args) {
    config("config", "config.yml") { AbyssFeatureConfig.serializer() }
    config("achievements", "achievements.yml") { AchievementsConfig.serializer() }
    config("anticheese", "anticheese.yml") { AntiCheeseConfig.serializer() }
    config("dialogs", "dialogs/config.yml") { DialogsConfig.serializer() }
    config("goals", "goals.yml") { GoalsConfig.serializer() }
    config("guilds", "guilds.yml") { GuildsConfig.serializer() }
    config("layers", "layers.yml") { LayersConfig.serializer() }
    config("loottables", "lootTables.yml") { LootCratesConfig.serializer() }
    config("music", "music.yml") { MusicConfig.serializer() }
    config("okibotravel", "okiboTravel.yml") { OkiboTravelConfig.serializer() }
    config("quests", "quests.yml") { QuestConfig.serializer() }
    config("respawn", "respawn.yml") { RespawnConfig.serializer() }
    config("triggerpoints", "triggerPoints/config.yml") { TriggerPointsConfig.serializer() }
    config("tutorial", "tutorial.yml") { Tutorial.serializer() }
    gearyPrefabs("mineinabyss", listOf("com.mineinabyss.components", "com.mineinabyss.features"))
}

// Some serializers touch Bukkit registries while initializing, which fails without a server
private fun SchemaWriter.config(name: String, path: String, serializer: () -> KSerializer<*>) =
    runCatching { schema("mineinabyss/$name", listOf("**/plugins/MineInAbyss/$path"), serializer()) }
        .onFailure { println("Warn: Could not generate schema mineinabyss/$name: $it") }
