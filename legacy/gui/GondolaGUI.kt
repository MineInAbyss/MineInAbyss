package com.derongan.minecraft.mineinabyss.gui

import com.derongan.minecraft.guiy.gui.ClickableElement
import com.derongan.minecraft.guiy.gui.Element
import com.derongan.minecraft.guiy.gui.FillableElement
import com.derongan.minecraft.guiy.gui.layouts.HistoryGuiHolder
import com.derongan.minecraft.guiy.helpers.toCell
import com.derongan.minecraft.guiy.kotlin_dsl.guiyLayout
import com.derongan.minecraft.guiy.kotlin_dsl.setElement
import com.derongan.minecraft.mineinabyss.MineInAbyss
import com.derongan.minecraft.mineinabyss.configuration.SpawnLocation
import com.derongan.minecraft.mineinabyss.configuration.SpawnLocationsConfig
import com.derongan.minecraft.mineinabyss.ecs.components.playerData
import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.derongan.minecraft.mineinabyss.world.layer
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.color
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*

class GondolaGUI(val player: Player) : HistoryGuiHolder(6, "Choose Spawn Location", mineInAbyss) {

    private fun buildMain() = guiyLayout {
        //TODO separate spawns into groups based on world
        setElement(0, 1, FillableElement(5, 9)) {
            addAll(SpawnLocationsConfig.data.spawns.map { parseLayer(it) })
        }
        addBackButton(this)
    }

    private fun parseLayer(spawnLoc: SpawnLocation): Element {
        val (loc, _, cost) = spawnLoc
        val displayItem = spawnLoc.displayItem.toItemStack()
        val balance = MineInAbyss.econ!!.getBalance(player) //TODO !!

        return if (balance >= cost) {
            displayItem.editItemMeta {
                lore = listOf(
                    "${ChatColor.RESET}Cost: ${ChatColor.GOLD}$$cost",
                    "${ChatColor.WHITE}You have: ${ChatColor.GOLD}$$balance"
                )
            }

            val button = ClickableElement(displayItem.toCell()) {
                val layer = loc.layer
                val playerData = player.playerData

                player.teleport(loc)
                player.sendTitle((layer?.name) ?: "Outside the abyss", (layer?.sub) ?: "A land of mystery", 50, 10, 20)

                MineInAbyss.econ!!.withdrawPlayer(player, cost.toDouble())
                //TODO start descent
//                playerData.descentDate = Date()
//                playerData.expOnDescent = playerData.exp
//                playerData.isIngame = true
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

    init {
        setElement(buildMain())
    }
}
