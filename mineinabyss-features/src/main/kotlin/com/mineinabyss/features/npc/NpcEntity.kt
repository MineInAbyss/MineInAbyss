package com.mineinabyss.features.npc

import com.mineinabyss.features.abyss
import com.mineinabyss.features.npc.action.DialogData
import com.mineinabyss.features.npc.action.DialogsConfig
import com.mineinabyss.features.npc.action.QuestDialogData
import com.mineinabyss.features.npc.shopkeeping.Trade
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.ticxo.modelengine.api.ModelEngineAPI
import net.kyori.adventure.text.Component
import net.minecraft.advancements.AdvancementRewards.Builder.recipe
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Interaction
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
        val chunk = location.chunk
        // delete the old entity if it exists and respawn a newer version instead
        // note: we can't respawn an entity a player is interacting with as it only triggers on chunk load (i.e. when no players are nearby)
        chunk.entities.forEach {
            if (config.id in it.scoreboardTags) {
                println("removed old npc entity with id ${config.id}")
                it.remove()
            } else {
                println("dialog config is ${dialogsConfig.configs.keys}")
                println("no old npc entity with id ${config.id} found")
            }
        }

        val entity = location.world.spawn(location, ItemDisplay::class.java)
        val modeledEntity = ModelEngineAPI.createModeledEntity(entity)
        val activeModel = ModelEngineAPI.createActiveModel(config.bbModel)
        modeledEntity.addModel(activeModel, true)

        entity.addScoreboardTag(config.id)
        entity.customName(config.displayName.miniMsg())
        entity.isCustomNameVisible = true
        entity.isPersistent = false
//        entity.isResponsive = true
        entity.teleport(location)
        val gearyEntity = entity.toGearyOrNull()?: return
        gearyEntity.set<Npc>(this@NpcEntity.config)

        if (dialogData != null) gearyEntity.set<DialogData>(this@NpcEntity.dialogData)
        else abyss.logger.w("Could not set dialog data for npc ${config.id}")

        if (questDialog != null) gearyEntity.set<QuestDialogData>(QuestDialogData(questDialog))
    }

    // everything else in this file is probably not gonna get used
    //-------------------------------------------
    fun createTypedNpc() {
        when (config.type) {
            "trader" -> createTraderNpc()
            "gondola_unlocker" -> createGondolaUnlockerNpc()
            "quest_giver" -> createQuestGiverNpc()
            "dialogue" -> createDialogueNpc()
            else -> throw IllegalArgumentException("Unknown type ${config.type}")
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
            val chunk = location.chunk

            // delete the old entity if it exists and respawn a newer version instead
            for (entity in chunk.entities) if (config.id in entity.scoreboardTags) entity.remove()
            val entity = location.world.spawn(location, Interaction::class.java)

            val recipes = createMerchantRecipes(config.tradeTable.trades)
            val merchant = Bukkit.createMerchant()

            merchant.recipes = recipes
            entity.addScoreboardTag(config.id)
            Bukkit.getPluginManager().registerEvents(object : Listener {
                @EventHandler
                fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
                    val player = event.player
                    player.info("bleh bleh bleh")
                    if (event.rightClicked == entity) {
                       player.info("bla bla bla")
                        event.isCancelled = true
                        merchant.recipes = createMerchantRecipes(config.tradeTable.trades)

                        MenuType.MERCHANT.builder().merchant(merchant).build(player).open()
                    }
                }
            }, abyss.plugin)
        }

        return
    }

    fun createMerchantRecipes(trades: List<Trade>): List<MerchantRecipe> {
        val gearyItems = mainWorld.toGeary().getAddon(ItemTracking)

        return trades.map { trade ->
            val inputItem = gearyItems.createItem(PrefabKey.of(trade.input.prefab))?.asQuantity(trade.input.amount)
                ?: error("Incorrect prefab key: ${trade.input.prefab}")
            val outputItem = gearyItems.createItem(PrefabKey.of(trade.output.prefab))?.asQuantity(trade.output.amount)
                ?: error("Incorrect prefab key: ${trade.output.prefab}")

            val recipe = MerchantRecipe(outputItem, 99999)
            recipe.apply { addIngredient(inputItem) }
        }
    }
}