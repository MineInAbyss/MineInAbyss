package com.mineinabyss.features.quests

import com.mineinabyss.features.abyss
import com.mineinabyss.features.quests.QuestManager.completeQuest
import com.mineinabyss.features.quests.QuestManager.getActiveQuests
import com.mineinabyss.features.quests.QuestManager.getCompletedQuests
import com.mineinabyss.features.quests.QuestManager.getVisitQuestProgress
import com.mineinabyss.features.quests.QuestManager.resetQuests
import com.mineinabyss.features.quests.QuestManager.unlockQuest
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.execute
import com.mineinabyss.geary.papermc.features.common.actions.DropItemsAction
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Configurable
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureWithContext
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners


class QuestFeature : FeatureWithContext<QuestFeature.Context>(::Context) {
    class Context : Configurable<QuestConfig> {
        override val configManager = config("quests", abyss.dataPath, QuestConfig())
        val questConfig by config("quests", abyss.dataPath, QuestConfig())
//        val database = Database(abyss.dataPath.resolve("quests.db").toString()) {
//            Table( """
//
//            """.trimIndent())
//
////            // Initialize tables if they don't exist
////            execute(
////                """
////                CREATE TABLE IF NOT EXISTS player_quests (
////                    player_uuid TEXT,
////                    quest_id TEXT,
////                    status TEXT,
////                    progress INTEGER,
////                    PRIMARY KEY (player_uuid, quest_id)
////                );
////                """.trimIndent()
////            )

    }

    override fun FeatureDSL.enable() {
        QuestConfigHolder.config = context.questConfig
        plugin.listeners(QuestListener(context.questConfig))

//        val player: Player = ////
//        val act = ActionGroupContext().apply { entity = player}
//        DropItemsAction(emptyList()).execute(act)



        mainCommand {
            "quests"(desc = "Commands for quests") {
                permission = "mineinabyss.quests"
                "unlock"(desc = "Unlocks a quest for a player") {
                    permission = "mineinabyss.quests.unlock"
                    val questId by stringArg()
                    playerAction {
                        if (questId in context.questConfig.visitQuests.keys) {
                            unlockQuest(player, questId)
                        } else {
                            player.error("Quest $questId not found")
                        }
                    }
                }
                "complete"(desc = "Completes a quest for a player") {
                    permission = "mineinabyss.quests.complete"
                    val questId by stringArg()
                    playerAction {
                        if (questId in context.questConfig.visitQuests.keys) {
                            completeQuest(player, questId)
                        } else {
                            player.error("Quest $questId not found")
                        }
                    }
                }
                "reset"(desc = "Resets all quests for a player") {
                    permission = "mineinabyss.quests.reset"
                    playerAction {
//                        context.questManager.completeQuest(...)
                        resetQuests(player)
                        player.success("All quests have been reset.")
                    }
                }
                "getProgressStatus"(desc = "Gets the progress status of a quest for a player") {
                    permission = "mineinabyss.quests.getProgressStatus"
                    val questId by stringArg()
                    playerAction {

                        when (questId) {
                            in getCompletedQuests(player) -> {
                                player.success("Quest $questId is completed.")
                            }
                            in getActiveQuests(player) -> {
                                val progress = getVisitQuestProgress(player, questId)
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
        tabCompletion {
            when (args.size) {
                1 -> listOf("quests").filter { it.startsWith(args[0], true) }
                2 -> if (args[0] == "quests") listOf("unlock", "complete", "reset", "getProgressStatus").filter { it.startsWith(args[1], true) } else null
                3 -> if (args[0] == "quests" && args[1] in listOf("unlock", "complete", "getProgressStatus")) {
                    context.questConfig.visitQuests.keys.filter { it.startsWith(args[2], true) }
                } else null
                else -> null
            }
        }
    }
}