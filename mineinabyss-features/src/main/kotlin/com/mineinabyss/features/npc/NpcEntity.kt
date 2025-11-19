package com.mineinabyss.features.npc

import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.ticxo.modelengine.api.ModelEngineAPI
import org.bukkit.World
import org.bukkit.entity.ItemDisplay

class NpcEntity(
    val config: Npc,
    val mainWorld: World,
    val dialogsConfig: DialogsConfig,
    val dialogData: DialogData? = dialogsConfig.configs[config.dialogId],
) {

    fun createBaseNpc() {
        val location = config.location
        val chunk = location.chunk

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
        entity.customName = config.displayName
        entity.isCustomNameVisible = true
        entity.isPersistent = false
        entity.teleport(location)
        val gearyEntity = entity.toGearyOrNull()?: return
        gearyEntity.set<Npc>(this@NpcEntity.config)
        if (dialogData != null) {
            gearyEntity.set<DialogData>(this@NpcEntity.dialogData)
        } else {
            error("couldnt set dialog data for npc ${config.id}")
        }
    }

    // everything else in this file is probably not gonna get used
    //-------------------------------------------
//    fun createTypedNpc() {
//        when (config.type) {
//            "trader" -> createTraderNpc()
//            "gondola_unlocker" -> createGondolaUnlockerNpc()
//            "quest_giver" -> createQuestGiverNpc()
//            "dialogue" -> createDialogueNpc()
//            else -> throw IllegalArgumentException("Unknown type ${config.type}")
//        }
//    }
//
//    fun createDialogueNpc() {
//
//    }
//
//    fun createQuestGiverNpc() {
//
//    }
//
//    fun createGondolaUnlockerNpc() {
//
//    }
//
//    fun createTraderNpc() {
//        if (config.tradeTable.trades.isNotEmpty()) {
//
//            val location = config.location
//            val chunk = location.chunk
//
//            for (e in chunk.entities) {
//                if (config.id in e.scoreboardTags) {
//                    // delete the old entity if it exists and respawn a newer version instead
//                    e.remove()
//                }
//            }
//            val entity = location.world.spawn(location, Interaction::class.java)
//
//            val recipes = createMerchantRecipes(config.tradeTable.trades)
//            val merchant = Bukkit.createMerchant()
//            //entity.setItemStack(ItemStack(Material.WITHER_SKELETON_SKULL))
//            //entity.setAI(false)
//            merchant.recipes = recipes
////            entity.profession = org.bukkit.entity.Villager.Profession.LIBRARIAN
////            entity.recipes = recipes
//            entity.customName = "a"
//            entity.addScoreboardTag("custom trade ig")
//            entity.addScoreboardTag(config.id)
//            Bukkit.getPluginManager().registerEvents(object : Listener {
//                @EventHandler
//                fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
//                    val player = event.player
//                    player.info("bleh bleh bleh")
//                    if (event.rightClicked == entity) {
//                       player.info("bla bla bla")
//                        event.isCancelled = true
//                        val new_recipe = createMerchantRecipes(config.tradeTable.trades)
//                        merchant.recipes = new_recipe
//
//                        MenuType.MERCHANT.builder().merchant(merchant).build(player).open()
//                    }
//                }
//            }, abyss.plugin)
//        }
//
//        return
//    }
//
//    fun createMerchantRecipes(trades: List<Trade>): List<MerchantRecipe> {
//        val gearyItems = mainWorld.toGeary().getAddon(ItemTracking)
//        val recipes = mutableListOf<MerchantRecipe>()
//        for (trade in trades) {
//            val inputItem: ItemStack = gearyItems.createItem(PrefabKey.Companion.of(trade.input.prefab)) ?: error("Incorrect prefab key: ${trade.input.prefab}")
//            inputItem.amount = trade.input.amount
//            val outputItem = gearyItems.createItem(PrefabKey.Companion.of(trade.output.prefab)) ?: error("Incorrect prefab key: ${trade.output.prefab}")
//            outputItem.amount = trade.output.amount
//
//            val recipe = MerchantRecipe(outputItem, 99999)
//            recipe.addIngredient(inputItem)
//
//            recipes.add(recipe)
//        }
//        return recipes
//    }
}