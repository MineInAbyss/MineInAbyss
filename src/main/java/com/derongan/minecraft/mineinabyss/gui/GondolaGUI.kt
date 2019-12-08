package com.derongan.minecraft.mineinabyss.gui

import com.derongan.minecraft.guiy.gui.*
import com.derongan.minecraft.guiy.gui.layouts.HistoryGuiHolder
import com.derongan.minecraft.mineinabyss.MineInAbyss
import com.derongan.minecraft.mineinabyss.getPlayerData
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class GondolaGUI(player: Player, plugin: MineInAbyss) : HistoryGuiHolder(6, "Choose Spawn Location", plugin) {
    private val context = MineInAbyss.getContext()

    private fun buildMain(): Layout? {
        val layout = Layout()
        val spawnLocConfig: FileConfiguration = context.configManager.startLocationCM

        //TODO separate spawns into groups based on world
        val grid = FillableElement(5, 9)
        val spawnLayers = spawnLocConfig.getMapList(SPAWN_KEY)
        spawnLayers.forEach { grid.addElement(parseLayer(it)) }
        layout.addElement(0, 1, grid)
        addBackButton(layout)
        return layout
    }

    private fun parseLayer(map: Map<*, *>): Element {
        val cost = if (map.containsKey(COST_KEY)) (map[COST_KEY] as String).toDouble() else 0.0
        val displayItem = (map[DISPLAY_ITEM_KEY] as ItemStack?)!!.clone()
        val itemMeta = displayItem.itemMeta!!
        val loc = map[LOCATION_KEY] as Location
        val balance = MineInAbyss.getEcon().getBalance(player)

        return if (balance >= cost) {
            itemMeta.lore = listOf("${ChatColor.GOLD}Cost: $$cost", "${ChatColor.WHITE}You have: ${ChatColor.GOLD}$balance")
            displayItem.itemMeta = itemMeta

            val button = ClickableElement(Cell.forItemStack(displayItem))
            button.setClickAction {
                val layer = MineInAbyss.getContext().getLayerForLocation(loc)
                val playerData = getPlayerData(player)

                player.teleport(loc)
                player.sendTitle(layer.name, layer.sub, 50, 10, 20)

                MineInAbyss.getEcon().withdrawPlayer(player, cost)

                playerData.descentDate = Date()
                playerData.expOnDescent = playerData.exp
                playerData.isIngame = true
                Bukkit.getScheduler().scheduleSyncDelayedTask(MineInAbyss.getInstance(), {
                    player.sendTitle("", String.format("%s%sLet the journey begin", ChatColor.GRAY, ChatColor.ITALIC), 30, 30, 20)
                }, 80)
            }
            button
        } else {
            displayItem.type = Material.BARRIER
            itemMeta.setDisplayName(ChatColor.STRIKETHROUGH.toString() + itemMeta.displayName)
            itemMeta.lore = listOf("${ChatColor.RED}Cannot Afford: ${ChatColor.GOLD}$$cost", "${ChatColor.RED}You have: ${ChatColor.GOLD}$balance")
            displayItem.itemMeta = itemMeta
            Cell.forItemStack(displayItem)
        }
    }

    companion object {
        const val SPAWN_KEY: String = "spawns"
        const val DISPLAY_ITEM_KEY: String = "display-item"
        private const val COST_KEY: String = "cost"
        private const val LOCATION_KEY: String = "location"
    }

    init {
        this.player = player
        setElement(buildMain())
    }
}