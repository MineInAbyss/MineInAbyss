package com.mineinabyss.features.achievements

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.achievements.data.AchievementStore
import com.mineinabyss.features.achievements.data.AdvancementStore
import com.mineinabyss.features.achievements.data.AdvancementSyncListener
import com.mineinabyss.features.goals.ConditionProgress
import com.mineinabyss.features.goals.goalListener.ClimbFactListener
import com.mineinabyss.features.goals.repository.GoalCache
import com.mineinabyss.features.goals.repository.GoalRepository
import com.mineinabyss.idofront.Idofront
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.suggests
import com.mineinabyss.idofront.datastore.setupDataStore
import com.mineinabyss.idofront.features.get
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.features.singleConfig
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import org.bukkit.Bukkit

val AchievementsFeature = module("achievements") {
    require(get<AbyssFeatureConfig>().achievements.enabled) { "Achievements feature is disabled" }
    singleConfig<AchievementsConfig>("achievements.yml") {
        default = AchievementsConfig()
        format = Yaml(configuration = YamlConfiguration(strictMode = false, polymorphismStyle = PolymorphismStyle.Property))
    }
    single { GoalCache() }
    single { GoalRepository(get<AchievementsConfig>().achievements.map { it.toGoal() }, get(), AchievementStore) }
    single {
        AchievementManager(get(), get()).also { manager ->
            manager.repository.onComplete = manager::onComplete
        }
    }
    Idofront.setupDataStore(AchievementStore)
    Idofront.setupDataStore(AdvancementStore)
    listeners(new(::AdvancementSyncListener), new(::AchievementListener))
    if (Bukkit.getPluginManager().isPluginEnabled("StaminaClimb")) {
        listeners(new(::ClimbFactListener))
    }
}.mainCommand {
    "achievement" {
        val achievementIdArg = { Args.greedyString().suggests { suggestFiltering(get<AchievementsConfig>().achievements.map { it.id }) } }

        "unlock" {
            executes.asPlayer().args("achievement" to achievementIdArg()) { id ->
                val achievement = get<AchievementsConfig>().byId(id) ?: fail("Unknown achievement $id")
                get<AchievementManager>().repository.forceComplete(player, achievement.toGoal())
                sender.success("Unlocked achievement ${achievement.name}")
            }
        }
        "reset" {
            executes.asPlayer().args("achievement" to achievementIdArg()) { id ->
                val achievement = get<AchievementsConfig>().byId(id) ?: fail("Unknown achievement $id")
                val manager = get<AchievementManager>()
                manager.repository.resetGoal(player, achievement.toGoal())
                manager.onReset(player, achievement.toGoal())
                sender.success("Reset progress for achievement ${achievement.name}")
            }
        }
        "status" {
            executes.asPlayer().args("achievement" to achievementIdArg()) { id ->
                val achievement = get<AchievementsConfig>().byId(id) ?: fail("Unknown achievement $id")
                val progress = get<AchievementManager>().repository.progress(player, achievement.id) ?: fail("Achievement ${achievement.name} not started${if (achievement.requires.isEmpty()) "" else " (requires ${achievement.requires.joinToString()})"}")
                val state = if (progress.completed) " (completed)" else ""
                sender.info("Progress for ${achievement.name}$state:")
                achievement.conditions.forEachIndexed { i, condition ->
                    sender.info(" - ${condition.describe(progress.conditions[i] ?: ConditionProgress())}")
                }
            }
            executes.asPlayer {
                val repository = get<AchievementManager>().repository
                val all = repository.allProgress(player)
                get<AchievementsConfig>().achievements.forEach { achievement ->
                    val progress = all[achievement.id]
                    val state = when {
                        progress == null -> "locked${if (achievement.requires.isEmpty()) "" else " (requires ${achievement.requires.joinToString()})"}"
                        progress.completed -> "completed"
                        else -> "in progress"
                    }
                    sender.info(" - ${achievement.id}: $state")
                }
            }
        }
    }
}
