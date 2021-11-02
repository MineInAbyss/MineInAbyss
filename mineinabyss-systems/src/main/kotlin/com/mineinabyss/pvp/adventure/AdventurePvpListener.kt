package com.mineinabyss.pvp.adventure

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.derongan.minecraft.deeperworld.world.section.inSectionOverlap
import com.mineinabyss.components.playerData
import com.mineinabyss.mineinabyss.isInHub
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class AdventurePvpListener : Listener {
    @EventHandler
    fun PlayerMoveEvent.promptPvpSelect() {
        val loc = player.location
        val data = player.playerData

        if (player.isInHub() && loc.inSectionOverlap && data.showPvPMessage && data.pvpUndecided) {
            player.sendMessage(
                """
                ${ChatColor.GREEN}Do you wish to enable PVP in the ${ChatColor.DARK_GREEN}${ChatColor.BOLD}Abyss?
                ${ChatColor.GREEN}Only players with PVP enabled can engage in combat with others.
                ${ChatColor.GREEN}This can only be changed in ${ChatColor.GOLD}${ChatColor.BOLD}Orth.
                """.trimIndent()
            )
            val yes = TextComponent("${ChatColor.DARK_GREEN}${ChatColor.BOLD}Yes")
            val no = TextComponent("${ChatColor.DARK_RED}${ChatColor.BOLD}No")

            player.sendMessage(yes)
            player.sendMessage(no)

            yes.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pvp on")
            no.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pvp off")
        }

    }

    @EventHandler
    fun PlayerDescendEvent.onEnterAbyss() {
        val data = player.playerData
        if (data.pvpUndecided) {
            data.pvpStatus = data.pvpStatus
            data.pvpUndecided = false
        }
    }

    @EventHandler
    fun PlayerAscendEvent.checkMessageToggle() {
        val data = player.playerData
        // If player hasn't toggled message off, set them as undecided
        if (data.showPvPMessage && player.isInHub()) data.pvpUndecided = true
    }
}