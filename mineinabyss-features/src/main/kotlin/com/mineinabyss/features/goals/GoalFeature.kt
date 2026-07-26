package com.mineinabyss.features.goals

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.goals.dataStore.GoalStore
import com.mineinabyss.features.goals.goalListener.ClimbFactListener
import com.mineinabyss.features.goals.goalListener.GoalListener
import com.mineinabyss.features.goals.repository.GoalCache
import com.mineinabyss.features.goals.repository.GoalRepository
import com.mineinabyss.geary.papermc.spawning.locations.RegionService
import com.mineinabyss.idofront.Idofront
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.suggests
import com.mineinabyss.idofront.datastore.setupDataStore
import com.mineinabyss.idofront.features.get
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.features.singleConfig
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.Services
import org.bukkit.Bukkit

val GoalFeature = module("goals") {
    require(get<AbyssFeatureConfig>().goals.enabled) { "Goals feature is disabled" }
    singleConfig<GoalsConfig>("goals.yml") {
        default = GoalsConfig()
        format = Yaml(configuration = YamlConfiguration(strictMode = false, polymorphismStyle = PolymorphismStyle.Property)) // allows for type: 'type'
    }
    single { GoalCache() }
    single { GoalRepository(get<GoalsConfig>().goals, get(), GoalStore) }
    Idofront.setupDataStore(GoalStore)
    listeners(new(::GoalListener))

    if (Bukkit.getPluginManager().isPluginEnabled("StaminaClimb")) {
        listeners(new(::ClimbFactListener))
    }
}.mainCommand {
    "goals" {
        val goalIdArg = { Args.string().suggests { suggestFiltering(get<GoalsConfig>().goals.map { it.id }) } }

        "start" {
            executes.asPlayer().args("goal" to goalIdArg()) { id ->
                val goal = get<GoalsConfig>().byId(id) ?: fail("Unknown goal $id")
                val repository = get<GoalRepository>()
                if (repository.startGoal(player, goal)) {
                    Services.getOrNull<RegionService>()?.let {
                        repository.seedRegions(player, it.regionsAt(player.location))
                    }
                    sender.success("Started goal ${goal.name}")
                } else sender.error("Goal ${goal.name} already started")
            }
        }
        "regions" {
            executes.asPlayer {
                val service = Services.getOrNull<RegionService>()
                    ?: fail("RegionService not registered, is Geary's locations feature loaded?")
                sender.info("Known regions (${service.regionIds.size}): ${service.regionIds.joinToString().ifEmpty { "none" }}")
                sender.info("You are inside: ${service.regionsAt(player.location).joinToString().ifEmpty { "none" }}")
            }
        }
        "unlock" {
            executes.asPlayer().args("goal" to goalIdArg()) { id ->
                val goal = get<GoalsConfig>().byId(id) ?: fail("Unknown goal $id")
                get<GoalRepository>().forceComplete(player, goal)
                sender.success("Unlocked goal ${goal.name}")
            }
        }
        "status" {
            executes.asPlayer().args("goal" to goalIdArg()) { id ->
                val goal = get<GoalsConfig>().byId(id) ?: fail("Unknown goal $id")
                val progress = get<GoalRepository>().progress(player, goal.id)
                    ?: fail("Goal ${goal.name} not started")
                val state = if (progress.completed) " (completed)" else ""
                sender.info("Progress for ${goal.name}$state:")
                goal.conditions.forEachIndexed { i, condition ->
                    sender.info(" - ${condition.describe(progress.conditions[i] ?: ConditionProgress())}")
                }
            }
            executes.asPlayer {
                val all = get<GoalRepository>().allProgress(player)
                if (all.isEmpty()) sender.info("No goals started.")
                else all.forEach { (id, progress) ->
                    val state = if (progress.completed) "completed" else "in progress"
                    sender.info(" - $id: $state")
                }
            }
        }
    }
}
