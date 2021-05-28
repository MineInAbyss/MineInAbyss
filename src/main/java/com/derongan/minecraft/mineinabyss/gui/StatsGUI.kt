package com.derongan.minecraft.mineinabyss.gui

import com.derongan.minecraft.deeperworld.world.section.section
import com.derongan.minecraft.guiy.gui.ClickableElement
import com.derongan.minecraft.guiy.gui.layouts.HistoryGuiHolder
import com.derongan.minecraft.guiy.helpers.toCell
import com.derongan.minecraft.guiy.kotlin_dsl.guiyLayout
import com.derongan.minecraft.mineinabyss.MineInAbyss
import com.derongan.minecraft.mineinabyss.configuration.ConfigConstants
import com.derongan.minecraft.mineinabyss.ecs.components.PlayerData
import com.derongan.minecraft.mineinabyss.ecs.components.playerData
import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.derongan.minecraft.mineinabyss.world.layer
import com.mineinabyss.geary.minecraft.access.geary
import com.mineinabyss.idofront.items.editItemMeta
import de.erethon.headlib.HeadLib
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import net.md_5.bungee.api.ChatColor as CC

class StatsGUI(private val player: Player) : HistoryGuiHolder(6, "Mine In Abyss - Stats", mineInAbyss) {
    private val col1 = ConfigConstants.mainColor
    private val col2 = ConfigConstants.secondaryColor
    private val mobConfigs: List<ClickableElement> = ArrayList()
    private val spawnList: List<ClickableElement> = ArrayList()
    private val playerData = player.playerData

    init {
        setElement(buildMain())
    }

    private fun buildMain() = guiyLayout {
        //Player head
        setElement(0, 0, getHead(player).toCell(col1.toString() + player.name))

        //The section the player is currently in
        val section = player.location.section
        val layerName = section?.let { section.layer?.name } ?: "Not in a layer"
        val sectionName = section?.key?.toString()?.uppercase() ?: "Not in a section"

        setElement(2, 0, HeadLib.QUARTZ_L.toItemStack().editItemMeta {
            setDisplayName("${col1}Layer: $col2$layerName")
            lore = listOf("${col1}Section: $col2$sectionName")
        }.toCell())

        //The player's level
        setElement(7, 0, Material.EXPERIENCE_BOTTLE.toCell("${col1}Level: ${CC.GREEN}${playerData.level}"))

        //The player's balance
        setElement(
            8, 0,
            Material.GOLD_BLOCK.toCell("${col1}Balance: ${CC.GOLD}$${MineInAbyss.econ!!.getBalance(player)}")
        )

        addBackButton(this)
    }

    private fun getHead(player: Player) = ItemStack(Material.PLAYER_HEAD).editItemMeta {
        if (this !is SkullMeta) return@editItemMeta
        setDisplayName("${CC.RESET}${player.name}")
        owningPlayer = player
    }
}
