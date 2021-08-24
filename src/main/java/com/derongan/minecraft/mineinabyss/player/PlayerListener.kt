package com.derongan.minecraft.mineinabyss.player

import com.derongan.minecraft.mineinabyss.AbyssContext
import com.derongan.minecraft.mineinabyss.AbyssContext.getPlayerData
import com.derongan.minecraft.mineinabyss.ascension.effect.effects.MaxHealthChangeEffect
import com.derongan.minecraft.mineinabyss.configuration.PlayerDataConfig
import com.derongan.minecraft.mineinabyss.playerData
import com.derongan.minecraft.mineinabyss.world.layer
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.messaging.color
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.logWarn
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerExpChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
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
    fun onPlayerDeath(pde: PlayerDeathEvent) {
        val player = pde.entity
        val playerData = getPlayerData(player)

        //TODO maybe limit this to only the survival server with a config option
        if (player.lastDamageCause?.cause == EntityDamageEvent.DamageCause.VOID) pde.keepInventory = true
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
    fun BlockPlaceEvent.place(){
        if(player.location.layer?.blockBlacklist?.contains(blockPlaced.type) == true){
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
        }
        else if (cause != EntityPotionEffectEvent.Cause.PLUGIN && cause != EntityPotionEffectEvent.Cause.COMMAND) {
            if (newEffect?.type == PotionEffectType.DAMAGE_RESISTANCE || newEffect?.type == PotionEffectType.SLOW_FALLING) {
                isCancelled = true
                player.error("The ${ChatColor.BOLD}Resistance Effect ${ChatColor.RED}has been disabled")
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerInteractEvent.onPlayerHarvest() {
        val block = clickedBlock
        var fortuneMultiplier = 0

        if (!hasBlock() || hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return

        val handItem = player.inventory.itemInMainHand

        if (handItem.type != Material.AIR && handItem.itemMeta.enchants.contains(Enchantment.LOOT_BONUS_BLOCKS)) {
            fortuneMultiplier = handItem.itemMeta.getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS)
        }

        val crops: Array<ItemStack> = when (block?.type) {
            Material.WHEAT -> arrayOf(ItemStack(Material.WHEAT, 1 + (Math.random() * (2 + fortuneMultiplier)).toInt()))
            Material.CARROTS -> arrayOf(ItemStack(Material.CARROT, 1 + (Math.random() * (2 + fortuneMultiplier)).toInt()))
            Material.POTATOES -> arrayOf(ItemStack(Material.POTATO, 1 + (Math.random() * (2 + fortuneMultiplier)).toInt()))
            Material.BEETROOTS -> arrayOf(ItemStack(Material.BEETROOT, 1 + (Math.random() * (2 + fortuneMultiplier)).toInt()))
            Material.NETHER_WART -> arrayOf(ItemStack(Material.NETHER_WART, 1 + (Math.random() * (2 + fortuneMultiplier)).toInt()))
            Material.COCOA -> arrayOf(ItemStack(Material.COCOA_BEANS, 1 + (Math.random() * (2 + fortuneMultiplier)).toInt()))
            else -> return
        }

        if(block.blockData !is Ageable) return

        val aging: Ageable = block.blockData as Ageable

        if (aging.age != aging.maximumAge) return

        val breakCrop = BlockBreakEvent(block, player)
        Bukkit.getPluginManager().callEvent(breakCrop)
        if (breakCrop.isCancelled) return

        aging.age = 0
        block.blockData = aging

        player.swingMainHand()

        for (drop in crops) { dropCrop(block.location, drop) }
    }

    private fun dropCrop(loc: Location, drop: ItemStack) {
        loc.world.dropItem(loc.add(Vector.getRandom().subtract(Vector(.5, .5, .5)).multiply(0.5)), drop).velocity =
            Vector.getRandom().add(Vector(-.5, +.5, -.5)).normalize().multiply(.15)
    }
}
