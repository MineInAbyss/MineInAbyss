package com.derongan.minecraft.mineinabyss.systems

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.mineinabyss.components.Orthbound
import com.derongan.minecraft.mineinabyss.configuration.MIAConfig
import com.mineinabyss.geary.minecraft.access.toGearyOrNull
import com.mineinabyss.geary.minecraft.components.Soulbound
import com.mineinabyss.geary.minecraft.store.encodeComponentsTo
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object SoulbindListener : Listener {
    @EventHandler
    fun PlayerAscendEvent.soulbindItems() {
        if (toSection != MIAConfig.data.hubSection) return
        player.inventory.contents.filterNotNull().forEach {
            val item = it.toGearyOrNull(player)
            item?.get<Orthbound>() ?: return@forEach
            item.set(Soulbound(owner = player.uniqueId))
            it.editItemMeta { lore = listOf("${ChatColor.GRAY}Soulbound", "") }
            it.itemMeta.lore.broadcastVal("lore: ")
            it.editMeta { }
            it.itemMeta.enchants.broadcastVal("enchants: ")
            item.get<Soulbound>()?.owner.broadcastVal("owner: ")
            item.encodeComponentsTo(it)
        }
    }

    @EventHandler
    fun PlayerDropItemEvent.dropSoulboundItem() {
        val item = itemDrop.toGearyOrNull()
        item?.get<Soulbound>() ?: return
        item.has<Soulbound>().broadcastVal("soulbound: ")
        item.get<Soulbound>()?.owner.broadcastVal("owner: ")
    }

    @EventHandler
    fun EntityPickupItemEvent.pickupSoulboundItem() {
        val player = entity as Player
        val items = item.toGearyOrNull()
        items?.get<Soulbound>() ?: return
        if (items.get<Soulbound>()?.owner != player.uniqueId) {
            item.itemStack.editItemMeta { lore = null }
            item.itemStack.itemMeta.lore.broadcastVal("lore: ")
        }
        items.has<Soulbound>().broadcastVal("soulbound: ")
        items.get<Soulbound>()?.owner.broadcastVal("owner: ")
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        player.inventory.contents.filterNotNull().forEach {
            val item = it.toGearyOrNull(player)
            item?.get<Soulbound>() ?: return
            item.has<Soulbound>().broadcastVal("soulbound: ")
            item.get<Soulbound>()?.owner.broadcastVal("owner: ")
        }
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        player.inventory.contents.filterNotNull().forEach {
            val item = it.toGearyOrNull(player)
            item?.get<Soulbound>() ?: return
            item.has<Soulbound>().broadcastVal("soulbound: ")
            item.get<Soulbound>()?.owner.broadcastVal("owner: ")
        }
    }
}