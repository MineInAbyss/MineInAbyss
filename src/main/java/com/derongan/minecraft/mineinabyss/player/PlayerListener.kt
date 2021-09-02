package com.derongan.minecraft.mineinabyss.player

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
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.messaging.color
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.logWarn
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerExpChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffectType
import java.io.IOException

object PlayerListener : Listener {
    @EventHandler
    fun PlayerJoinEvent.onPlayerJoin() {
        AbyssContext.playerDataMap[player.uniqueId] = PlayerDataConfig.loadPlayerData(player)
    }

    @EventHandler
    fun PlayerQuitEvent.onPlayerLeave() {
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
            logWarn("Failed to save data for player ${player.uniqueId}")
            e.printStackTrace()
        }
    }

    @EventHandler
    fun PlayerDeathEvent.onPlayerDeath() {
        val playerData = getPlayerData(entity)
        val cause = entity.lastDamageCause?.cause

        //TODO maybe limit this to only the survival server with a config option
        if (cause == EntityDamageEvent.DamageCause.VOID) keepInventory = true
        if (!playerData.isIngame) return
        playerData.isIngame = false
        entity.sendMessage(
            """
            &6&lGame Stats:
            &6Exp earned:&7 ${playerData.exp - playerData.expOnDescent}
            &6Started dive on:&7 ${playerData.descentDate}
            """.trimIndent().color()
        )
    }

    @EventHandler
    fun PlayerExpChangeEvent.onPlayerGainEXP() {
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
        }
    }

}
