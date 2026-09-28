package com.mineinabyss.features.misc

import com.mineinabyss.components.displaylocker.LockDisplayItem
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.nexo.NexoCustomBlock
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.entityOfOrNull
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.idofront.plugin.Plugins
import com.nexomc.nexo.api.NexoBlocks
import nl.rutgerkok.blocklocker.BlockLockerAPIv2
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Block
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

class MiscListener(private val config: MiscConfig) : Listener {
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
                p.playEffect(newBlock.location, Effect.BONE_MEAL_USE, null)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockFertilizeEvent.onGrowGrass() {
        val bonemeal = config.grassBonemeal
        if (block.type != Material.GRASS_BLOCK) return

        val tops = blocks.filter { (it.blockData as? Bisected)?.half == Bisected.Half.TOP }.associateBy { it.location }
        blocks.filter { it.location !in tops && Random.nextDouble() < bonemeal.replaceChance }.forEach { state ->
            val id = bonemeal.randomBlock() ?: return
            val top = tops[state.location.add(0.0, 1.0, 0.0)]
            if (!state.block.placeBonemealBlock(id)) return@forEach
            blocks.remove(state)
            top?.let(blocks::remove)
        }
    }

    private fun Block.placeBonemealBlock(block: Any): Boolean = when (block) {
        is PrefabKey -> {
            if (!Plugins.isEnabled("Nexo")) return false
            val mechanic = withGeary { entityOfOrNull(block)?.get<NexoCustomBlock>()?.mechanic(block) } ?: return false
            NexoBlocks.place(mechanic.itemID, location)
            true
        }
        is String -> {
            val data = runCatching { Bukkit.createBlockData(block) }.getOrNull() ?: return false
            if (data !is Bisected) {
                blockData = data
                return true
            }

            val above = getRelative(BlockFace.UP)
            if (!above.type.isAir) return false
            setBlockData(data.apply { half = Bisected.Half.BOTTOM }, false)
            above.setBlockData((data.clone() as Bisected).apply { half = Bisected.Half.TOP }, false)
            true
        }
        else -> false
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
