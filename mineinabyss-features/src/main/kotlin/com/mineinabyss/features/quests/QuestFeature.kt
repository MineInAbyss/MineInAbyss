package com.mineinabyss.features.quests

import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.features.get
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.features.singleConfig
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success


val QuestFeature = module("quests") {
    require(get<AbyssFeatureConfig>().quests.enabled) { "Quest feature is disabled" }
    val config by singleConfig<QuestConfig>("quests.yml")
    single { new(::QuestManager) }

    listeners(new(::QuestListener))
}.mainCommand {
    "quests" {
        description = "Commands for quests"
        permission = "mineinabyss.quests"
        "unlock" {
            description = "Unlocks a quest for a player"
            permission = "mineinabyss.quests.unlock"

            executes.asPlayer().args("quest" to Args.string()) { questId ->
                if (questId in get<QuestConfig>().visitQuests.keys) {
                    get<QuestManager>().unlockQuest(player, questId)
                } else {
                    player.error("Quest $questId not found")
                }
            }
        }
        "complete" {
            description = "Completes a quest for a player"
            permission = "mineinabyss.quests.complete"

            executes.asPlayer().args("quest" to Args.string()) { questId ->
                if (questId in get<QuestConfig>().visitQuests.keys) {
                    get<QuestManager>().completeQuest(player, questId)
                } else {
                    player.error("Quest $questId not found")
                }
            }
        }
        "reset" {
            description = "Resets all quests for a player"
            permission = "mineinabyss.quests.reset"
            executes.asPlayer {
//                        context.questManager.completeQuest(...)
                get<QuestManager>().resetQuests(player)
                player.success("All quests have been reset.")
            }
        }
        "getProgressStatus" {
            description = "Gets the progress status of a quest for a player"
            permission = "mineinabyss.quests.getProgressStatus"
            executes.asPlayer().args("quest" to Args.string()) { questId ->
                val manager = get<QuestManager>()
                when (questId) {
                    in manager.completedQuests(player) -> {
                        player.success("Quest $questId is completed.")
                    }

                    in manager.activeQuests(player) -> {
                        val progress = manager.visitQuestProgress(player, questId)
                        player.success("Quest $questId is in progress. Progress: ${progress.first}/${progress.second}")
                    }

                    else -> {
                        player.success("Quest $questId is not started.")
                    }
                }
            }
        }
    }
}