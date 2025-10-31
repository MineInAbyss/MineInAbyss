package com.mineinabyss.features.npc

import com.mineinabyss.features.abyss
import com.mineinabyss.features.npc.Npc.NPCType
import com.mineinabyss.features.npc.action.DialogData
import com.mineinabyss.features.npc.action.DialogsConfig
import com.mineinabyss.features.npc.shopkeeping.Trade
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Interaction
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
    val dialogData: DialogData? = dialogsConfig.configs[config.dialogId]
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
                println("no old npc entity with id ${config.id} found")
            }
        }

        val entity = location.world.spawn(location, Interaction::class.java)
        entity.addScoreboardTag(config.id)
        entity.customName(config.displayName.miniMsg())
        entity.isCustomNameVisible = true
        entity.isPersistent = false
        entity.isResponsive = true
        val gearyEntity = entity.toGearyOrNull()?: return
        gearyEntity.set<Npc>(this@NpcEntity.config)
        if (dialogData != null) {
            gearyEntity.set<DialogData>(this@NpcEntity.dialogData)
            println("dialogg data set")
        } else {
            println("couldnt set dialog data for npc ${config.id}")
        }

    }

    // everything else in this file is probably not gonna get used
    //-------------------------------------------
    fun createTypedNpc() {
        when (config.type) {
            NPCType.TRADER -> createTraderNpc()
            NPCType.GONDOLA_UNLOCKER -> createGondolaUnlockerNpc()
            NPCType.QUEST_GIVER -> createQuestGiverNpc()
            NPCType.DIALOGUE -> createDialogueNpc()
        }
    }

    fun createDialogueNpc() {

    }

    fun createQuestGiverNpc() {

    }

    fun createGondolaUnlockerNpc() {

    }

    fun createTraderNpc() {
        if (config.tradeTable.trades.isEmpty()) return
        val location = config.location

        // delete the old entity if it exists and respawn a newer version instead
        location.chunk.entities.filter { config.id in it.scoreboardTags }.forEach(Entity::remove)
        val entity = location.spawn<Interaction> {
            customName(Component.text("a"))
            addScoreboardTag("custom trade ig")
            addScoreboardTag(config.id)
        }

        val recipes = createMerchantRecipes(config.tradeTable.trades)
        val merchant = Bukkit.createMerchant().apply { this.recipes = recipes }

        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun PlayerInteractEntityEvent.onPlayerInteractEntity() {
                if (rightClicked.uniqueId != entity?.uniqueId) return
                isCancelled = true
                merchant.recipes = createMerchantRecipes(config.tradeTable.trades)

                MenuType.MERCHANT.builder().merchant(merchant).build(player).open()
            }
        }, abyss.plugin)

        return
    }

    fun createMerchantRecipes(trades: List<Trade>): List<MerchantRecipe> {
        val gearyItems = mainWorld.toGeary().getAddon(ItemTracking)

        return trades.map { trade ->
            val inputItem: ItemStack = gearyItems.createItem(trade.input.prefab)?.asQuantity(trade.input.amount)
                ?: error("Incorrect prefab key: ${trade.input.prefab}")
            val outputItem = gearyItems.createItem(trade.output.prefab)?.asQuantity(trade.output.amount)
                ?: error("Incorrect prefab key: ${trade.output.prefab}")

            MerchantRecipe(outputItem, 99999).apply { addIngredient(inputItem) }
        }
    }
}