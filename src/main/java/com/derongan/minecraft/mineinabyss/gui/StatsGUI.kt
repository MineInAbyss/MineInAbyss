package com.derongan.minecraft.mineinabyss.gui

import com.derongan.minecraft.guiy.gui.ClickableElement
import com.derongan.minecraft.guiy.gui.layouts.HistoryGuiHolder
import com.derongan.minecraft.guiy.helpers.toCell
import com.derongan.minecraft.guiy.kotlin_dsl.guiyLayout
import com.derongan.minecraft.mineinabyss.AbyssContext
import com.derongan.minecraft.mineinabyss.MineInAbyss
import com.derongan.minecraft.mineinabyss.configuration.ConfigConstants
import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.derongan.minecraft.mineinabyss.player.PlayerData
import com.mineinabyss.idofront.items.editItemMeta
import de.erethon.headlib.HeadLib
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*
import net.md_5.bungee.api.ChatColor as CC

class StatsGUI(private val player: Player) : HistoryGuiHolder(6, "Mine In Abyss - Stats", mineInAbyss) {
    private val col1 = ConfigConstants.mainColor
    private val col2 = ConfigConstants.secondaryColor
    private val context: AbyssContext = MineInAbyss.getContext()
    private val mobConfigs: List<ClickableElement> = ArrayList()
    private val spawnList: List<ClickableElement> = ArrayList()
    private val playerData: PlayerData = context.getPlayerData(player)


    init {
        setElement(buildMain())
    }

    private fun buildMain() = guiyLayout {
        //Player head
        setElement(0, 0, getHead(player).toCell(col1.toString() + player.name))
        //Whistle
        val whistleItem = playerData.whistle.item
        setElement(1, 0, whistleItem.toCell(col1.toString() + "Whisle: " + whistleItem.itemMeta?.displayName))

        //The section the player is currently in
        val section = context.realWorldManager.getSectionFor(player.location)
        val layerName = section?.let { context.worldManager.getLayerForSection(section).name } ?: "Not in a layer"
        val sectionName = section?.key?.toString()?.toUpperCase() ?: "Not in a section"

        setElement(2, 0, HeadLib.QUARTZ_L.toItemStack().editItemMeta {
            setDisplayName("${col1}Layer: $col2$layerName")
            lore = listOf("${col1}Section: $col2$sectionName")
        }.toCell())

        //The player's level
        setElement(7, 0, Material.EXPERIENCE_BOTTLE.toCell("${col1}Level: ${CC.GREEN}${context.getPlayerData(player).level}"))

        //The player's balance
        setElement(8, 0, Material.GOLD_BLOCK.toCell("${col1}Balance: ${CC.GOLD}$${MineInAbyss.getEcon().getBalance(player)}"))

        addBackButton(this)
    }

    private fun getHead(player: Player) = ItemStack(Material.PLAYER_HEAD).editItemMeta {
        if (this !is SkullMeta) return@editItemMeta
        setDisplayName("${CC.RESET}${player.name}")
        owningPlayer = player
    }
}