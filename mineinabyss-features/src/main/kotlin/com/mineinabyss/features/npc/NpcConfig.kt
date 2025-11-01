package com.mineinabyss.features.npc

import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.gondolas.Ticket
import com.mineinabyss.features.gondolas.pass.TicketConfigHolder
import com.mineinabyss.features.gondolas.pass.isRouteUnlocked
import com.mineinabyss.features.gondolas.pass.unlockRoute
import com.mineinabyss.features.npc.NpcAction.DialogData
import com.mineinabyss.features.npc.NpcAction.QuestDialogData
import com.mineinabyss.features.npc.shopkeeping.TradeTable
import com.mineinabyss.features.quests.QuestManager
import com.mineinabyss.features.quests.QuestManager.checkAndCompleteQuest
import com.mineinabyss.features.quests.QuestManager.isQuestCompleted
import com.mineinabyss.features.quests.QuestManager.playerHasCompletedQuest
import com.mineinabyss.features.quests.QuestManager.playerHasUnlockedQuest
import com.mineinabyss.features.quests.QuestManager.unlockQuest
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.idofrontLogger
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.Vector3fSerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.MenuType
import org.joml.Vector3f

@Serializable
data class Npc(
    val id: String,
    val customName: String,
    val location: @Serializable(LocationSerializer::class) Location,
    val scale: @Serializable(Vector3fSerializer::class) Vector3f,
    val bbModel: String,
    val tradeTable: TradeTable, // todo: remove
    val tradeTableId: String, // use id pulled from config instead to be more modular
    val type: Type, // "trader", "gondola_unlocker", "quest_giver", "dialogue"
    @EncodeDefault(EncodeDefault.Mode.NEVER) val dialogId: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val ticketId: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val questId: String? = null,
    val questEndId: String? = null, // basically the dialog of id to use when talking to the npc after unlocking the quest; idk what i was cooking but this aint it
) {

    enum class Type {
        TRADER, GONDOLA_UNLOCKER, QUEST_GIVER, DIALOGUE
    }


    fun defaultInteraction(player: Player, dialogId: String, dialogData: DialogData, questDialogData: QuestDialogData) {
        when (type) {
            Type.TRADER -> traderInteraction(player)
            Type.GONDOLA_UNLOCKER -> dialogInteraction(player, dialogId, dialogData)
            Type.QUEST_GIVER -> questGiverInteraction(player, questDialogData, dialogData)
            Type.DIALOGUE -> dialogInteraction(player, dialogId, dialogData)
        }
    }

    fun fallbackInteraction(player: Player) {
        when (type) {
            Type.TRADER -> traderInteraction(player)
            Type.GONDOLA_UNLOCKER -> gondolaUnlockerInteraction(player)
            Type.QUEST_GIVER -> questGiverInteraction(player, null)
            Type.DIALOGUE -> player.error("dialog data missing for $id")
        }
    }


    // On the quest behaviour:
    // each npc of type "quest" have 2 dialogs associated with them, one "regular" dialog which contains the quest offer and the action to unlock it
    // and another dialog, which will be triggered subsequently.
    // this second dialog is represented by questEndId and is stored under questDialogData on the geary entity.
    // So when a player interact with a quest npc for the first time, if he accepts the quest, he will then see the dialog associated with questEndId.
    // This dialog should be something along the line of "come back when you've done X", with 2 options :
    // - "I did it!"
    // - "Goodbye"
    // should the player select I did it, the npc will then run the quest completion check and give rewards if they are any.
    // Or return an error in chat if the player hasn't completed the quest yet.
    // this is really messy, and it could definitely use a "quest progress dialog" and a "quest end dialog".
    fun questGiverInteraction(player: Player, questDialogData: QuestDialogData? = null, dialogData: DialogData? = null) {
        questDialogData ?: return
        questId ?: return
        when {
            // Quest is done, reward is claimed
            playerHasCompletedQuest(player, questId) -> player.info("You have already completed this quest.")
            // Quest is done but reward not claimed yet
            isQuestCompleted(player, questId) -> questDialogData.dialogData.startDialogue(player, questId, this)
            // Quest is started but not completed
            playerHasUnlockedQuest(player, questId) -> player.error("Come back when you've completed this quest: ${QuestManager.questInformation(player, questId)}")
            // Quest is not started
            dialogData != null && !playerHasUnlockedQuest(player, questId) -> dialogData.startDialogue(player, dialogId ?: return, this)
        }
    }


    fun traderInteraction(player: Player) {
        if (tradeTable.trades.isEmpty()) return
        val merchant = Bukkit.createMerchant().apply { recipes = tradeTable.createMerchantRecipes() ?: return }
        MenuType.MERCHANT.builder().merchant(merchant).build(player).open()
    }

    fun dialogInteraction(player: Player, dialogId: String, dialogData: DialogData) {
        dialogData.startDialogue(player, dialogId, this)
    }


    fun gondolaUnlockerInteraction(player: Player) {
        // instead of printing messages in chat, we should open an error dialog instead
        ticketId ?: return idofrontLogger.e { "Ticket id is null for gondola unlocker NPC $id" }
        val ticket: Ticket = TicketConfigHolder.config?.tickets?.get(ticketId)
            ?: return idofrontLogger.e("Ticket with id $ticketId not found")
        player.editPlayerData {
            when {
                player.isRouteUnlocked(ticket) -> player.error("You already own this ticket!")
                ticket.ticketPrice > orthCoinsHeld -> player.error("You do not have enough orth coins to purchase this ticket. (Price: ${ticket.ticketPrice}, You have: $orthCoinsHeld)")
                else -> {
                    orthCoinsHeld -= ticket.ticketPrice
                    player.unlockRoute(ticket)
                    player.success("Obtained the ${ticket.ticketName} ticket")
                }
            }
        }
    }

    fun questCompleteInteraction(player: Player, qid: String) {
        if (questId == null) {
            player.error("Missing questId")
            return
        }
        checkAndCompleteQuest(player, qid)
    }

    fun questUnlockInteraction(player: Player, qid: String) {
        if (questId == null) {
            player.error("Missing questId")
            return
        }
        unlockQuest(player, qid)
        // this one is a bit more finicky, tho I think i'll do something like:
        // give the player a "objective" component, which would be something like:
        // (quest_id | progress | max_progress | completion_action)
        // so most of them are straightforward, and completion_action would be like an event listener of a specific type
        // so I guess it would also be like another object like
        // (event_type | event_data), where type could be like "kill" and data would be like "mob", so when we register our quest listeners,
        // we would do something like :
        // onMobKill
        // killer = player
        // if (killer has objective component with event_type "kill" and event_data == mob)
        // objective.progress++
        // same for every quest type we support
        // then we could also have a "success" function, the listener would invoke if objective.progress >= objective.max_progress
        // and it would go like, player.objective.displaysuccessmessage or some variation of that
    }

}

@Serializable
class NpcsConfig(
    val npcs: Map<String, Npc> = mapOf(),
)
