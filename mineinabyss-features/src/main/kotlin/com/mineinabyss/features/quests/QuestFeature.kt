package com.mineinabyss.features.quests

import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import org.koin.core.module.dsl.scopedOf


val QuestFeature = feature("quests") {
    scopedModule {
        scoped<QuestConfig> { config("quests", abyss.dataPath, QuestConfig()).getOrLoad() }
        scopedOf(::QuestManager)
        scopedOf(::QuestListener)
    }

    onEnable {
        listeners(get<QuestListener>())
//        val player: Player = ////
//        val act = ActionGroupContext().apply { entity = player}
//        DropItemsAction(emptyList()).execute(act)
    }

    mainCommand {
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
                        in manager.getCompletedQuests(player) -> {
                            player.success("Quest $questId is completed.")
                        }

                        in manager.getActiveQuests(player) -> {
                            val progress = manager.getVisitQuestProgress(player, questId)
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
}