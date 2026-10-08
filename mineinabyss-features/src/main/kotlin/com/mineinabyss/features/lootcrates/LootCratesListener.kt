package com.mineinabyss.features.lootcrates

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.features.abyss
import com.mineinabyss.features.lootcrates.constants.LootCratePermissions
import com.mineinabyss.features.lootcrates.database.CrateLoot
import com.mineinabyss.features.lootcrates.database.LootCratesDataStore
import com.mineinabyss.features.lootcrates.database.decodeLeftovers
import com.mineinabyss.features.lootcrates.database.encodeLeftovers
import com.mineinabyss.idofront.datastore.launchWrite
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.time.ticks
import com.nexomc.nexo.api.events.furniture.NexoFurnitureBreakEvent
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent
import com.nexomc.nexo.utils.drops.Drop
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration

class LootCratesListener(
    val msg: LootCratesConfig.Messages,
    val lootCrates: LootCrates,
    val cache: LootedCache,
    val editMode: LootCrateEditMode,
    val overrides: CrateOverrides,
) : Listener, AutoCloseable {
    private val lastInteractTick = HashMap<UUID, Int>()
    private val restockTasks = HashMap<UUID, MutableMap<UUID, Job>>()
    private val pendingSaves = HashSet<LootInventory>()

    // Before furniture is sent to the joining player, so it already shows what they looted
    @EventHandler(priority = EventPriority.LOWEST)
    fun PlayerJoinEvent.loadLooted() = load(player)

    fun load(player: Player) {
        cache.load(player)
        cache.all(player).forEach { (crate, state) -> scheduleRestock(player, crate, state) }
    }

    @EventHandler
    fun PlayerQuitEvent.unloadLooted() {
        cache.unload(player)
        editMode.leave(player)
        lastInteractTick.remove(player.uniqueId)
        restockTasks.remove(player.uniqueId)?.values?.forEach { it.cancel() }
    }

    // Nexo handles the interaction at HIGHEST, so cancelling here stops storage, seats and state cycling
    @EventHandler(priority = EventPriority.HIGH)
    fun NexoFurnitureInteractEvent.onCrateInteract() {
        val loot = baseEntity.containsLoot ?: return
        isCancelled = true
        if (hand != EquipmentSlot.HAND) return

        // Nexo can fire this more than once for a single click
        val tick = Bukkit.getCurrentTick()
        if (lastInteractTick.put(player.uniqueId, tick) == tick) return

        overrides.cover(mechanic.itemID)

        if (player in editMode) {
            if (loot.isCustomLoot()) player.openInventory(CustomLootEditor(baseEntity).inventory)
            else lootCrates.preview(player, loot, baseEntity)
            return
        }

        if (!player.hasPermission(LootCratePermissions.OPEN)) return player.error(msg.noPermissionToOpen)
        if (!cache.isLoaded(player)) return player.error(msg.lootNotLoaded)

        val now = Clock.System.now()
        val restock = lootCrates.settings(loot.table).restock
        val current = cache.current(player, baseEntity.uniqueId, restock)
        if (current?.emptied == true) {
            val center = baseEntity.location.toCenterLocation()
            player.spawnParticle(Particle.SMOKE, center, 12, 0.2, 0.2, 0.2, 0.02)
            player.playSound(lootCrates.sounds(mechanic.itemID).emptySound, center.x, center.y, center.z)
            return
        }

        val contents = current?.leftovers?.decodeLeftovers()
            ?: lootCrates.roll(loot, baseEntity)
            ?: return player.error(msg.tableNotFound.format(loot.table))

        // Saved before opening, so a disconnect cannot hand out a second roll
        if (current == null) {
            cache.save(player, baseEntity.uniqueId, CrateLoot(now, loot.table, contents.encodeLeftovers()))
            // A reroll after restocking takes the crate out of its looted look
            baseEntity.resendTo(player)
        }

        val looting = LootInventory.Looting(baseEntity.uniqueId, loot.table, current?.rolledAt ?: now)
        lootCrates.openLoot(player, loot, baseEntity, contents, looting)
    }

    @EventHandler
    fun InventoryCloseEvent.closeLoot() {
        val loot = inventory.getHolder(false) as? LootInventory ?: return
        loot.crate?.playCrateSound(loot.sounds.closeSound) ?: player.playSound(loot.sounds.closeSound)

        val looting = loot.looting ?: return
        val player = player as? Player ?: return
        val state = saveLeftovers(player, loot, looting)
        loot.crate?.resendTo(player)
        scheduleRestock(player, looting.crateId, state)
    }

    // Leftovers are otherwise only saved on close, so a crash would hand back everything taken since opening
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun InventoryClickEvent.saveTakenLoot() {
        val loot = view.topInventory.getHolder(false) as? LootInventory ?: return
        val player = whoClicked as? Player ?: return
        val looting = loot.looting ?: return
        if (!pendingSaves.add(loot)) return

        // The click only moves items after its event finishes
        abyss.launch {
            delay(1.ticks)
            pendingSaves.remove(loot)
            if (player.openInventory.topInventory.getHolder(false) === loot) saveLeftovers(player, loot, looting)
        }
    }

    private fun saveLeftovers(player: Player, loot: LootInventory, looting: LootInventory.Looting): CrateLoot =
        CrateLoot(looting.rolledAt, looting.table, loot.inventory.contents.toList().encodeLeftovers())
            .also { cache.save(player, looting.crateId, it) }

    /** Overrides only run when a crate is sent, so an emptied crate is resent once it restocks or it would keep looking looted */
    private fun scheduleRestock(player: Player, crateId: UUID, state: CrateLoot) {
        restockTasks[player.uniqueId]?.remove(crateId)?.cancel()
        if (!state.emptied) return

        val restock = lootCrates.settings(state.table).restock ?: return
        val untilRestock = restock - (Clock.System.now() - state.rolledAt)
        if (untilRestock <= Duration.ZERO) return
        restockTasks.getOrPut(player.uniqueId, ::HashMap)[crateId] = abyss.launch {
            // Always suspends at least once, so the job is stored before it can remove itself
            delay(untilRestock + 1.ticks)
            restockTasks[player.uniqueId]?.remove(crateId, coroutineContext.job)
            (Bukkit.getEntity(crateId) as? ItemDisplay)?.resendTo(player)
        }
    }

    // Leftovers are saved, so anything put in would turn the crate into personal storage
    @EventHandler(ignoreCancelled = true)
    fun InventoryClickEvent.onlyTakeLoot() {
        val top = view.topInventory.takeIf { it.getHolder(false) is LootInventory } ?: return
        isCancelled = when (clickedInventory) {
            top -> action in PUTTING_ACTIONS || action == InventoryAction.HOTBAR_SWAP && swappedItem()?.isEmpty == false
            null -> false
            else -> action == InventoryAction.MOVE_TO_OTHER_INVENTORY
        }
    }

    private fun InventoryClickEvent.swappedItem() = with(whoClicked.inventory) {
        if (hotbarButton >= 0) getItem(hotbarButton) else itemInOffHand
    }

    @EventHandler(ignoreCancelled = true)
    fun InventoryDragEvent.onlyTakeLoot() {
        if (view.topInventory.getHolder(false) !is LootInventory) return
        if (rawSlots.any { it < view.topInventory.size }) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun NexoFurnitureBreakEvent.onCrateBreak() {
        if (baseEntity.containsLoot == null) return
        if (player !in editMode) {
            player.error(if (player.hasPermission(LootCratePermissions.EDIT)) msg.editModeRequired else msg.noPermissionToBreak)
            isCancelled = true
            return
        }

        // Crates are handed out with /mia lootcrates menu, breaking one should not leave a copy behind
        drop = Drop.emptyDrop()
        val crate = baseEntity.uniqueId
        lootCrates.closeViewers(crate)
        cache.forgetAll(crate)
        player.launchWrite { LootCratesDataStore.deleteCrate(crate) }
    }

    override fun close() {
        restockTasks.values.flatMap { it.values }.forEach(Job::cancel)
        restockTasks.clear()
    }

    private companion object {
        val PUTTING_ACTIONS = setOf(
            InventoryAction.PLACE_ALL,
            InventoryAction.PLACE_ONE,
            InventoryAction.PLACE_SOME,
            InventoryAction.PLACE_ALL_INTO_BUNDLE,
            InventoryAction.PLACE_SOME_INTO_BUNDLE,
            InventoryAction.SWAP_WITH_CURSOR,
        )
    }
}
