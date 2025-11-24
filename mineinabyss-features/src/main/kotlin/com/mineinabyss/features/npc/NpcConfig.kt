package com.mineinabyss.features.npc

import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.gondolas.Ticket
import com.mineinabyss.features.gondolas.pass.TicketConfigHolder
import com.mineinabyss.features.gondolas.pass.isRouteUnlocked
import com.mineinabyss.features.gondolas.pass.unlockRoute
import com.mineinabyss.features.npc.shopkeeping.TradeTable
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.idofrontLogger
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
) {


    fun defaultInteraction(player: Player, dialogId: String, dialogData: DialogData, questDialogData: QuestDialogData) {
        when (type) {
            "trader" -> traderInteraction(player)
            "gondola_unlocker" -> dialogInteraction(player, dialogId, dialogData)
//            "quest_giver" -> questGiverInteraction(player, questDialogData, dialogData)
            "dialogue" -> dialogInteraction(player, dialogId, dialogData)
            else -> throw IllegalArgumentException("Unknown NPC type: $type")
        }
    }

    fun fallbackInteraction(player: Player) {
        when (type) {
            "trader" -> traderInteraction(player)
//            "gondola_unlocker" -> gondolaUnlockerInteraction(player)
//            "quest_giver" -> questGiverInteraction(player)
            "dialogue" -> player.sendMessage("dialog data missing")
            else -> throw IllegalArgumentException("Unknown NPC type: $type")
        }
    }


//    fun questGiverInteraction(player: Player, questDialogData: QuestDialogData? = null, dialogData: DialogData? = null) {
//        questDialogData ?: return
//        questId ?: return
//        TODO("Reimplement with DI")
////        when {
////            // Quest is done, reward is claimed
////            playerHasCompletedQuest(player, questId) -> {
////                player.info("You have already completed this quest.")
////            }
////
////            // Quest is done but reward not claimed yet
////            isQuestCompleted(player, questId) -> {
////                questDialogData.dialogData.startDialogue(player, questId, this)
////            }
////
////            // Quest is started but not completed
////            playerHasUnlockedQuest(player, questId) -> {
////                player.error("Come back when you've completed this quest: ${QuestManager.getQuestInformation(player, questId)}")
////            }
////
////            // Quest is not started
////            dialogData != null && !playerHasUnlockedQuest(player, questId) -> {
////                dialogData.startDialogue(player, dialogId ?: return, this)
////            }
////        }
//    }


    // TODO: update
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


    fun gondolaUnlockerInteraction(player: Player, ticketId: String) {
        // instead of printing messages in chat, we should open an error dialog instead
        // require adding support for redirects most likely
//        ticketId ?: return idofrontLogger.e { "Ticket id is null for gondola unlocker NPC $id" }
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

//    fun questCompleteInteraction(player: Player, qid: String) {
//        if (questId == null) {
//            player.error("Missing questId")
//            return
//        }
//    }

//    fun questUnlockInteraction(player: Player, qid: String) {
//        if (questId == null) {
//            player.error("Missing questId")
//            return
//        }
//    }

}

@Serializable
class NpcsConfig(
    val npcs: Map<String, Npc> = mapOf(),
)
