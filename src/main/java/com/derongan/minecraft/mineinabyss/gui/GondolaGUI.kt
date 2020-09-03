package com.derongan.minecraft.mineinabyss.gui

import com.derongan.minecraft.guiy.gui.ClickableElement
import com.derongan.minecraft.guiy.gui.Element
import com.derongan.minecraft.guiy.gui.FillableElement
import com.derongan.minecraft.guiy.gui.layouts.HistoryGuiHolder
import com.derongan.minecraft.guiy.helpers.toCell
import com.derongan.minecraft.guiy.kotlin_dsl.guiyLayout
import com.derongan.minecraft.guiy.kotlin_dsl.setElement
import com.derongan.minecraft.mineinabyss.AbyssContext
import com.derongan.minecraft.mineinabyss.MineInAbyss
import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.derongan.minecraft.mineinabyss.playerData
import com.derongan.minecraft.mineinabyss.world.layer
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.color
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class GondolaGUI(val player: Player) : HistoryGuiHolder(6, "Choose Spawn Location", mineInAbyss) {

    private fun buildMain() = guiyLayout {
        //TODO separate spawns into groups based on world
        setElement(0, 1, FillableElement(5, 9)){
            val spawnLocConfig: FileConfiguration = AbyssContext.configManager.startLocationCM
            val spawnLayers = spawnLocConfig.getMapList(SPAWN_KEY)
            addAll(spawnLayers.map { parseLayer(it) })
        }
        addBackButton(this)
    }

    private fun parseLayer(map: Map<*, *>): Element {
        val cost = if (map.containsKey(COST_KEY)) (map[COST_KEY] as String).toDouble() else 0.0
        val displayItem = (map[DISPLAY_ITEM_KEY] as ItemStack?)!!.clone()
        val loc = map[LOCATION_KEY] as Location
        val balance = MineInAbyss.econ!!.getBalance(player)

        return if (balance >= cost) {
            displayItem.editItemMeta {
                lore = listOf("${ChatColor.RESET}Cost: ${ChatColor.GOLD}$$cost", "${ChatColor.WHITE}You have: ${ChatColor.GOLD}$$balance")
            }

            val button = ClickableElement(displayItem.toCell()) {
                val layer = loc.layer
                val playerData = player.playerData

                player.teleport(loc)
                player.sendTitle((layer?.name) ?: "Outside the abyss", (layer?.sub) ?: "A land of mystery", 50, 10, 20)

                MineInAbyss.econ?.withdrawPlayer(player, cost)

                playerData.descentDate = Date()
                playerData.expOnDescent = playerData.exp
                playerData.isIngame = true
                Bukkit.getScheduler().scheduleSyncDelayedTask(mineInAbyss, {
                    player.sendTitle("", "${ChatColor.GRAY}${ChatColor.ITALIC}Let the journey begin", 30, 30, 20)
                }, 80)
            }
            button
        } else {
            displayItem.type = Material.BARRIER
            displayItem.editItemMeta {
                setDisplayName("&m$displayName".color())
                lore = listOf("&cCannot Afford: &6$$cost", "&cYou have: &6$$balance".color())
            }
            displayItem.toCell()
        }
    }

    companion object {
        const val SPAWN_KEY: String = "spawns"
        const val DISPLAY_ITEM_KEY: String = "display-item"
        private const val COST_KEY: String = "cost"
        private const val LOCATION_KEY: String = "location"
    }

    init {
        setElement(buildMain())
    }
}