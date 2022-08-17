package com.mineinabyss.misc

import com.mineinabyss.components.displaylocker.LockDisplayItem
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.entities.rightClicked
import nl.rutgerkok.blocklocker.BlockLockerAPIv2
import org.bukkit.Material
import org.bukkit.block.Lectern
import org.bukkit.block.data.type.RespawnAnchor
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.entity.ThrownPotion
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTakeLecternBookEvent
import org.bukkit.potion.PotionEffectType

class MiscListener : Listener {

    @EventHandler
    fun ProjectileHitEvent.onDouseItemFrame() {
        val entity = entity as? ThrownPotion ?: return
        val player = entity.shooter as? Player ?: return
        if (hitEntity !is ItemFrame) return
        if (entity.potionMeta.basePotionData.type.effectType != PotionEffectType.INVISIBILITY) return
        hitEntity?.location?.getNearbyEntitiesByType(ItemFrame::class.java, 1.0)?.forEach { frame ->
            val lockable = frame.toGeary().get<LockDisplayItem>()
            if (lockable?.lockState == true && player.uniqueId !in lockable.allowedAccess) return@forEach
            frame.isVisible = false
        }
    }

    @EventHandler
    fun PlayerInteractEvent.onInteractAnchor() {
        val data = clickedBlock?.blockData as? RespawnAnchor ?: return
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (data.charges >= data.maximumCharges || item?.type != Material.GLOWSTONE) isCancelled = true
    }

    @EventHandler
    fun PrepareAnvilEvent.removeAnvilMaxRepairCost() {
        inventory.maximumRepairCost = 10000
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onInteractPrivatedLectern() {
        val block = clickedBlock ?: return
        if (rightClicked && block.type == Material.LECTERN && BlockLockerAPIv2.isProtected(block))
            player.openInventory((block.state as Lectern).inventory)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerTakeLecternBookEvent.onTakeBookPrivatedLectern() {
        if (!BlockLockerAPIv2.isAllowed(player, lectern.block, true))
            isCancelled = true
    }
}
