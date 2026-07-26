package com.mineinabyss.features.quests

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.features.AbyssFeatureConfig
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


val QuestFeature = module("quests") {
    require(get<AbyssFeatureConfig>().quests.enabled) { "Quest feature is disabled" }
    singleConfig<QuestConfig>("quests.yml") {
        default = QuestConfig()
        format = Yaml(configuration = YamlConfiguration(strictMode = false, polymorphismStyle = PolymorphismStyle.Property)) // allows for type: 'type'
    }
    single { GoalCache() }
    single { GoalRepository(get<QuestConfig>().quests.map { it.toGoal() }, get(), QuestStore) }
    single {
        QuestManager(get(), get()).also { manager ->
            manager.repository.onComplete = { player, goal ->
                player.success("Quest '${goal.name}' completed!")
            }
        }
    }
    Idofront.setupDataStore(QuestStore)
    listeners(new(::QuestListener))
    if (Bukkit.getPluginManager().isPluginEnabled("StaminaClimb")) {
        listeners(new(::ClimbFactListener))
    }
}.mainCommand {
    "quests" {
        description = "Commands for quests"
        permission = "mineinabyss.quests"
        val questIdArg = { Args.string().suggests { suggestFiltering(get<QuestConfig>().quests.map { it.id }) } }

        "unlock" {
            description = "Unlocks a quest for a player"
            permission = "mineinabyss.quests.unlock"
            executes.asPlayer().args("quest" to questIdArg()) { questId ->
                get<QuestConfig>().byId(questId) ?: fail("Quest $questId not found")
                get<QuestManager>().unlockQuest(player, questId)
            }
        }
        "complete" {
            description = "Completes a quest for a player"
            permission = "mineinabyss.quests.complete"
            executes.asPlayer().args("quest" to questIdArg()) { questId ->
                get<QuestConfig>().byId(questId) ?: fail("Quest $questId not found")
                get<QuestManager>().completeQuest(player, questId)
            }
        }
        "remove" {
            description = "Deletes a quest's progress for a player (back to not started)"
            permission = "mineinabyss.quests.remove"
            executes.asPlayer().args("quest" to questIdArg()) { questId ->
                val quest = get<QuestConfig>().byId(questId) ?: fail("Quest $questId not found")
                get<QuestManager>().removeQuest(player, questId)
                player.success("Removed quest ${quest.name}")
            }
        }
        "reset" {
            description = "Resets a quest's progress for a player (stays active)"
            permission = "mineinabyss.quests.reset"
            executes.asPlayer().args("quest" to questIdArg()) { questId ->
                val quest = get<QuestConfig>().byId(questId) ?: fail("Quest $questId not found")
                get<QuestManager>().resetQuest(player, questId)
                player.success("Reset progress for quest ${quest.name}")
            }
        }
        "clear" {
            description = "Deletes all quest progress for a player"
            permission = "mineinabyss.quests.clear"
            executes.asPlayer {
                get<QuestManager>().clearQuests(player)
                player.success("All quests have been removed.")
            }
        }
        "status" {
            description = "Gets the progress status of a quest for a player"
            permission = "mineinabyss.quests.status"
            executes.asPlayer().args("quest" to questIdArg()) { questId ->
                val quest = get<QuestConfig>().byId(questId) ?: fail("Quest $questId not found")
                val manager = get<QuestManager>()
                val progress = manager.repository.progress(player, questId)
                    ?: fail("Quest ${quest.name} not started")
                val state = when {
                    progress.rewardClaimed -> " (completed, reward claimed)"
                    progress.completed -> " (completed, reward unclaimed)"
                    else -> ""
                }
                sender.info("Progress for ${quest.name}$state:")
                quest.conditions.forEachIndexed { i, condition ->
                    sender.info(" - ${condition.describe(progress.conditions[i] ?: ConditionProgress())}")
                }
            }
            executes.asPlayer {
                val all = get<QuestManager>().repository.allProgress(player)
                if (all.isEmpty()) sender.info("No quests started.")
                else all.forEach { (id, progress) ->
                    val state = when {
                        progress.rewardClaimed -> "completed, reward claimed"
                        progress.completed -> "completed, reward unclaimed"
                        else -> "in progress"
                    }
                    sender.info(" - $id: $state")
                }
            }
        }
    }
}
