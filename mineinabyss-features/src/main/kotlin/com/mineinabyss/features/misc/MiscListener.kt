package com.mineinabyss.features.misc

import com.destroystokyo.paper.MaterialSetTag
import com.mineinabyss.components.displaylocker.LockDisplayItem
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.entities.rightClicked
import nl.rutgerkok.blocklocker.BlockLockerAPIv2
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Lectern
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.RespawnAnchor
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.entity.ThrownPotion
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockFertilizeEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerEggThrowEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTakeLecternBookEvent
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

class MiscListener : Listener {
    @EventHandler
    fun ProjectileHitEvent.onDouseItemFrame() {
        val entity = entity as? ThrownPotion ?: return
        val player = entity.shooter as? Player ?: return
        if (hitEntity !is ItemFrame) return
        if (PotionEffectType.INVISIBILITY !in (entity.potionMeta.basePotionType?.potionEffects?.map { it.type } ?: emptyList())) return
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
    fun PlayerInteractEvent.onBoneMealDirt() {
        val (block, item) = (clickedBlock ?: return) to (item ?: return)
        if (action != Action.RIGHT_CLICK_BLOCK || useInteractedBlock() == Event.Result.DENY) return
        if (block.type != Material.DIRT || item.type != Material.BONE_MEAL) return

        if (player.gameMode != GameMode.CREATIVE) item.subtract()
        setUseInteractedBlock(Event.Result.DENY)

        for (x in -7..7) for (y in 0..5) for (z in -7..7) {
            val newBlock = block.location.clone().add(x.toDouble(), y.toDouble(), z.toDouble()).block
            if (newBlock.type != Material.DIRT) continue
            newBlock.type = Material.GRASS_BLOCK
            if (Random.nextDouble() < 0.5) newBlock.location.getNearbyPlayers(16.0).forEach { p ->
                p.playEffect(newBlock.location, org.bukkit.Effect.BONE_MEAL_USE, 1)
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockFertilizeEvent.onGrowGrass() {
        if (block.type != Material.GRASS_BLOCK) return

        val blockedFlowers = setOf(Material.MANGROVE_PROPAGULE)
        val allowedFlowers = MaterialSetTag.FLOWERS.values.filter { it !in blockedFlowers }
        val rareFlowers = setOf(Material.SPORE_BLOSSOM, Material.WITHER_ROSE)

        blocks.removeIf { (it.blockData as? Bisected)?.half == Bisected.Half.TOP }
        blocks.forEach { state ->
            val newPlant = allowedFlowers.filter { if (state.blockData is Bisected) it.createBlockData() is Bisected else it.createBlockData() !is Bisected }.random().createBlockData()
            if ((Random.nextDouble() > (if (newPlant.material in rareFlowers) 0.05 else 0.7))) return@forEach

            (newPlant as? Bisected)?.also { it.half = Bisected.Half.BOTTOM }?.let {
                state.block.getRelative(BlockFace.UP).state.blockData = it.apply { it.half = Bisected.Half.TOP }
            }
            state.blockData = newPlant
            state.update(true, true)
        }
    }

    @EventHandler
    fun PrepareAnvilEvent.removeAnvilMaxRepairCost() {
        view.maximumRepairCost = 10000
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun PlayerInteractEvent.onInteractPrivatedLectern() {
        val (block, state) = (clickedBlock ?: return) to (clickedBlock!!.state as? Lectern ?: return)
        if (!rightClicked || !BlockLockerAPIv2.isProtected(block)) return

        if (item?.type == Material.WRITABLE_BOOK || item?.type == Material.WRITTEN_BOOK) {
            if (BlockLockerAPIv2.isAllowed(player, block, true)) return
            else if (state.inventory.isEmpty) player.openBook(item ?: return)
        } else if (!state.inventory.isEmpty) player.openInventory(state.inventory)
        isCancelled = true // Prevent "denied" message
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerTakeLecternBookEvent.onTakeBookPrivatedLectern() {
        if (!BlockLockerAPIv2.isAllowed(player, lectern.block, true))
            isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerEggThrowEvent.onEggThrow() {
        isHatching = false
    }
}
