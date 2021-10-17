package com.derongan.minecraft.mineinabyss.player

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.deeperworld.world.section.inSectionOverlap
import com.derongan.minecraft.mineinabyss.AbyssContext
import com.derongan.minecraft.mineinabyss.AbyssContext.getPlayerData
import com.derongan.minecraft.mineinabyss.ascension.effect.effects.MaxHealthChangeEffect
import com.derongan.minecraft.mineinabyss.configuration.MIAConfig
import com.derongan.minecraft.mineinabyss.configuration.PlayerDataConfig
import com.derongan.minecraft.mineinabyss.harvestPlant
import com.derongan.minecraft.mineinabyss.playerData
import com.derongan.minecraft.mineinabyss.world.layer
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.messaging.color
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.logWarn
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffectType
import java.io.IOException


object PlayerListener : Listener {
    @EventHandler
    fun onPlayerJoin(joinEvent: PlayerJoinEvent) {
        AbyssContext.playerDataMap[joinEvent.player.uniqueId] = PlayerDataConfig.loadPlayerData(joinEvent.player)
    }

    @EventHandler
    fun onPlayerLeave(playerQuitEvent: PlayerQuitEvent) {
        val (player) = playerQuitEvent
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.run {
            modifiers.filter {
                it.name == MaxHealthChangeEffect.CURSE_MAX_HEALTH
                        && !MaxHealthChangeEffect.activeEffects.contains(it)
            }.forEach {
                removeModifier(it)
            }
        }

        val data: PlayerData = AbyssContext.playerDataMap.remove(player.uniqueId) ?: return
        try {
            PlayerDataConfig.savePlayerData(data)
        } catch (e: IOException) {
            logWarn("Failed to save data for player ${playerQuitEvent.player.uniqueId}")
            e.printStackTrace()
        }
    }

    @EventHandler
    fun PlayerDeathEvent.onPlayerDeath() {
        val player = entity
        val playerData = getPlayerData(player)

        if (!playerData.keepInvStatus) {
            entity.inventory.contents.filterNotNull().forEach { player.world.dropItemNaturally(player.location, it) }
            player.inventory.clear()
        }

        //TODO maybe limit this to only the survival server with a config option
        if (player.lastDamageCause?.cause == EntityDamageEvent.DamageCause.VOID) keepInventory = true
        if (!playerData.isIngame) return
        playerData.isIngame = false
        player.sendMessage(
            """
            &6&lGame Stats:
            &6Exp earned:&7 ${playerData.exp - playerData.expOnDescent}
            &6Started dive on:&7 ${playerData.descentDate}
            """.trimIndent().color()
        )
    }

    @EventHandler
    fun onPlayerGainEXP(e: PlayerExpChangeEvent) {
        val (player, amount) = e
        if (amount <= 0) return
        AbyssContext.econ?.depositPlayer(player, amount.toDouble())
        player.playerData.addExp(amount.toDouble())
    }

    @EventHandler
    fun BlockPlaceEvent.place() {
        if (player.location.layer?.blockBlacklist?.contains(blockPlaced.type) == true) {
            player.error("You may not place this block on this layer.")
            isCancelled = true
        }
    }

    @EventHandler
    fun EntityPotionEffectEvent.onPlayerHit() {
        val player = entity as? Player ?: return
        if (cause == EntityPotionEffectEvent.Cause.MILK) {
            isCancelled = true
            player.error("${ChatColor.BOLD}Milk ${ChatColor.RED}has been disabled")
        } else if (cause != EntityPotionEffectEvent.Cause.PLUGIN && cause != EntityPotionEffectEvent.Cause.COMMAND) {
            if (newEffect?.type == PotionEffectType.DAMAGE_RESISTANCE || newEffect?.type == PotionEffectType.SLOW_FALLING) {
                isCancelled = true
                player.error("The ${ChatColor.BOLD}Resistance Effect ${ChatColor.RED}has been disabled")
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerInteractEvent.onPlayerHarvest() {
        val block = clickedBlock ?: return

        if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return

        if (harvestPlant(block, player)) {
            player.swingMainHand()
            block.world.playSound(block.location, Sound.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0f, 2.0f)
        }
    }

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
            player.playerData.pvpStatus && attacker.playerData.pvpStatus
            && player.uniqueId != attacker.uniqueId //
        ) return

        isCancelled = true
    }
}
