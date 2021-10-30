package com.mineinabyss.pvp.adventure

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.deeperworld.world.section.inSectionOverlap
import com.mineinabyss.components.playerData
import com.mineinabyss.mineinabyss.core.MIAConfig
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerMoveEvent

class AdventurePvpListener : Listener {
    @EventHandler
    fun PlayerMoveEvent.onLeaveOrth() {
        val loc = player.location
        val data = player.playerData

        if (MIAConfig.data.hubSection == WorldManager.getSectionFor(loc) && loc.inSectionOverlap && data.showPvPMessage) {
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

            yes.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pvpon")
            no.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pvpoff")
            data.showPvPMessage = false
        }

    }

    //If Player doesn't choose, and hasn't chosen before
    @EventHandler
    fun PlayerDescendEvent.onEnterAbyss() {
        val data = player.playerData

        if (data.pvpUndecided) {
            player.performCommand("pvpoff")
            data.showPvPMessage = false //Negates a repeated message due to no decision
        }
    }

    @EventHandler
    fun PlayerAscendEvent.onEnterOrth() {
        val data = player.playerData
        data.showPvPMessage = data.pvpUndecided
    }

    @EventHandler
    fun EntityDamageByEntityEvent.playerCombatSystem() {
        val player = entity as? Player ?: return

        val attacker: Player = when (damager) {
            is Projectile -> {
                (damager as Projectile).shooter as? Player ?: return
            }
            is Player -> {
                (damager as Player)
            }
            else -> {
                return
            }
        }

        if (
            (player.playerData.pvpStatus && attacker.playerData.pvpStatus)
            || player == attacker
        ) return

        isCancelled = true
    }
}