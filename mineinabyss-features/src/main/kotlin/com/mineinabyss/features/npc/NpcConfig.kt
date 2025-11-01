package com.mineinabyss.features.npc

import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.gondolas.Ticket
import com.mineinabyss.features.gondolas.pass.TicketConfigHolder
import com.mineinabyss.features.gondolas.pass.isRouteUnlocked
import com.mineinabyss.features.gondolas.pass.unlockRoute
import com.mineinabyss.features.npc.NpcAction.DialogData
import com.mineinabyss.features.npc.NpcAction.QuestDialogData
import com.mineinabyss.features.npc.shopkeeping.TradeTable
import com.mineinabyss.features.quests.QuestManager.checkAndCompleteQuest
import com.mineinabyss.features.quests.QuestManager.completeQuest
import com.mineinabyss.features.quests.QuestManager.playerHasCompletedQuest
import com.mineinabyss.features.quests.QuestManager.playerHasUnlockedQuest
import com.mineinabyss.features.quests.QuestManager.unlockQuest
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.idofrontLogger
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.serialization.LocationSerializer
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.MenuType

@Serializable
data class Npc(
    val id: String,
    val displayName: String,
    val location: @Serializable(LocationSerializer::class) Location,
    val scale: List<Double>, // ??
    val bbModel: String, // unsure, actually I guess it would be serializable
    val tradeTable: TradeTable, // todo: remove
    val tradeTableId: String, // use id pulled from config instead to be more modular
    val type: String, // "trader", "gondola_unlocker", "quest_giver", "dialogue"
    val dialogId: String? = null,
    val ticketId: String? = null,
    val questId : String? = null,
    val questEndId: String? = null, // basically the dialog of id to use when talking to the npc after unlocking the quest; idk what i was cooking but this aint it
) {



    fun defaultInteraction(player: Player, dialogId: String, dialogData: DialogData, questDialogData: QuestDialogData) {
        when (type) {
            "trader" -> traderInteraction(player)
            "gondola_unlocker" -> dialogInteraction(player, dialogId, dialogData)
            "quest_giver" -> questGiverInteraction(player, questDialogData, dialogData)
            "dialogue" -> dialogInteraction(player, dialogId, dialogData)
            else -> throw IllegalArgumentException("Unknown NPC type: $type")
        }
    }

    fun fallbackInteraction(player: Player) {
        when (type) {
            "trader" -> traderInteraction(player)
            "gondola_unlocker" -> gondolaUnlockerInteraction(player)
            "quest_giver" -> questGiverInteraction(player)
            "dialogue" -> player.sendMessage("dialog data missing")
            else -> throw IllegalArgumentException("Unknown NPC type: $type")
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
        if (questDialogData != null && playerHasUnlockedQuest(player, questId ?: return)) {
            questDialogData.dialogData.startDialogue(player, questId, this)
        } else if (questDialogData != null && dialogData != null && !playerHasCompletedQuest(player, questId ?: return)) {
            dialogData.startDialogue(player, dialogId ?: return, this)
//            questUnlockInteraction(player)
        }
        if (playerHasCompletedQuest(player, questId ?: return)) {
            player.info("You have already completed this quest.")
            return
        }
    }


    fun traderInteraction(player: Player) {
        if (tradeTable.trades.isEmpty()) return
        val recipes = tradeTable.createMerchantRecipes() ?: return
        val merchant = Bukkit.createMerchant()
        merchant.recipes = recipes
        MenuType.MERCHANT.builder().merchant(merchant).build(player).open()
        return
    }

    fun dialogInteraction(player: Player, dialogId: String, dialogData: DialogData) {
        dialogData.startDialogue(player, dialogId, this)
    }


    fun gondolaUnlockerInteraction(player: Player) {
        // instead of printing messages in chat, we should open an error dialog instead
        ticketId ?: return idofrontLogger.e{"Ticket id is null for gondola unlocker NPC $id"}
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
    val npcs: Map<String, Npc> = mapOf()
)
