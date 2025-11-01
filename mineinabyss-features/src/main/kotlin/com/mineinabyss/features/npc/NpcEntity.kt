package com.mineinabyss.features.npc

import com.mineinabyss.features.abyss
import com.mineinabyss.features.npc.NpcAction.DialogData
import com.mineinabyss.features.npc.NpcAction.DialogsConfig
import com.mineinabyss.features.npc.NpcAction.QuestDialogData
import com.mineinabyss.features.npc.shopkeeping.Trade
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.ticxo.modelengine.api.ModelEngineAPI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.bukkit.inventory.MerchantRecipe

class NpcEntity(
    val config: Npc,
    val mainWorld: World,
    val dialogsConfig: DialogsConfig,
    val dialogData: DialogData? = dialogsConfig.configs[config.dialogId],
    val questDialog: DialogData? = dialogsConfig.configs[config.questEndId]
) {

    fun createBaseNpc() {
        val location = config.location
        // delete the old entity if it exists and respawn a newer version instead
        // note: we can't respawn an entity a player is interacting with as it only triggers on chunk load (i.e. when no players are nearby)
        location.chunk.entities.forEach {
            if (config.id in it.scoreboardTags) {
                println("removed old npc entity with id ${config.id}")
                it.remove()
            } else {
                println("dialog config is ${dialogsConfig.configs.keys}")
                println("no old npc entity with id ${config.id} found")
            }
        }

        val entity = location.spawn<ItemDisplay> {
            addScoreboardTag(config.id)
            customName(config.customName.miniMsg())
            isCustomNameVisible = true
            isPersistent = false
        }
        val gearyEntity = entity?.toGearyOrNull()?: return
        gearyEntity.set<Npc>(this@NpcEntity.config)

        val modeledEntity = ModelEngineAPI.createModeledEntity(entity)
        val activeModel = ModelEngineAPI.createActiveModel(config.bbModel)
        modeledEntity.addModel(activeModel, true)

        //TODO Remove debug
        if (dialogData != null) {
            gearyEntity.set<DialogData>(this@NpcEntity.dialogData)
            println("dialogg data set")
        } else {
            println("couldnt set dialog data for npc ${config.id}")
        }

        if (questDialog != null) {
            gearyEntity.set<QuestDialogData>(QuestDialogData(questDialog))
        }

    }

    // everything else in this file is probably not gonna get used
    //-------------------------------------------
    fun createTypedNpc() {
        when (config.type) {
            Npc.Type.TRADER -> createTraderNpc()
            Npc.Type.GONDOLA_UNLOCKER -> createGondolaUnlockerNpc()
            Npc.Type.QUEST_GIVER -> createQuestGiverNpc()
            Npc.Type.DIALOGUE -> createDialogueNpc()
        }
    }

    fun createDialogueNpc() {

    }

    fun createQuestGiverNpc() {

    }

    fun createGondolaUnlockerNpc() {

    }

    fun createTraderNpc() {
        if (config.tradeTable.trades.isNotEmpty()) {
            val location = config.location
            // delete the old entity if it exists and respawn a newer version instead
            location.chunk.entities.filter { config.id in it.scoreboardTags }.forEach(Entity::remove)
            val entity = location.spawn<ItemDisplay> {
                addScoreboardTag(config.id)
                addScoreboardTag("custom trade ig")
                customName(Component.text("a"))
            } ?: return

            val recipes = createMerchantRecipes(config.tradeTable.trades)
            val merchant = Bukkit.createMerchant()
            //entity.setItemStack(ItemStack(Material.WITHER_SKELETON_SKULL))
            //entity.setAI(false)
            merchant.recipes = recipes
//            entity.profession = org.bukkit.entity.Villager.Profession.LIBRARIAN
//            entity.recipes = recipes
            //TODO This should not be a listener that is registered and never unregistered
            Bukkit.getPluginManager().registerEvents(object : Listener {
                @EventHandler
                fun PlayerInteractEntityEvent.onPlayerInteractEntity() {
                    if (rightClicked.uniqueId != entity.uniqueId) return
                    isCancelled = true
                    merchant.recipes = createMerchantRecipes(config.tradeTable.trades)

                    MenuType.MERCHANT.builder().merchant(merchant).build(player).open()
                }
            }, abyss.plugin)
        }

        return
    }

    fun createMerchantRecipes(trades: List<Trade>): List<MerchantRecipe> {
        val gearyItems = mainWorld.toGeary().getAddon(ItemTracking)
        return trades.map { trade ->
            val inputItem: ItemStack = gearyItems.createItem(trade.input.prefab) ?: error("Incorrect prefab key: ${trade.input.prefab}")
            val outputItem = gearyItems.createItem(trade.output.prefab) ?: error("Incorrect prefab key: ${trade.output.prefab}")
            inputItem.amount = trade.input.amount
            outputItem.amount = trade.output.amount

            MerchantRecipe(outputItem, 99999).apply { addIngredient(inputItem) }
        }
    }
}