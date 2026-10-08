package com.mineinabyss.features.lootcrates

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.components.lootcrates.ContainsLoot
import com.mineinabyss.components.lootcrates.CrateSounds
import com.mineinabyss.components.lootcrates.LootCrateConstants
import com.mineinabyss.components.lootcrates.LootCrateSettings
import com.mineinabyss.components.lootcrates.LootEntry
import com.mineinabyss.components.lootcrates.LootTable
import com.mineinabyss.components.lootcrates.nexoId
import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.abyss
import com.mineinabyss.features.lootcrates.constants.LootCratePermissions
import com.mineinabyss.features.lootcrates.database.LootCratesDataStore
import com.mineinabyss.features.lootcrates.database.decodeLeftovers
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.PrefabLoading
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.toEntityOrNull
import com.mineinabyss.geary.papermc.tracking.GearyArgs
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.guiy.canvas.guiy
import com.mineinabyss.idofront.Idofront
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.ArgsMinecraft
import com.mineinabyss.idofront.commands.brigadier.context.IdoCommandContext
import com.mineinabyss.idofront.commands.brigadier.map
import com.mineinabyss.idofront.commands.brigadier.oneOf
import com.mineinabyss.idofront.datastore.launchWrite
import com.mineinabyss.idofront.datastore.readBlocking
import com.mineinabyss.idofront.datastore.setupDataStore
import com.mineinabyss.idofront.features.get
import com.mineinabyss.idofront.features.launch
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.features.requirePlugins
import com.mineinabyss.idofront.features.singleConfig
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.time.ticks
import com.nexomc.nexo.api.NexoFurniture
import com.nexomc.nexo.api.NexoItems
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemRarity
import org.bukkit.inventory.ItemStack
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.toKotlinUuid

@Serializable
class LootCratesConfig(val messages: Messages = Messages()) {
    @Serializable
    class Messages(
        val tableNotFound: String = "Could not find loot table %s. Please let an administrator know!",
        val noPermissionToEdit: String = "You don't have permission to edit loot crates",
        val noPermissionToOpen: String = "You don't have permission to open loot crates",
        val noPermissionToBreak: String = "You don't have permission to break loot crates, ask a member of staff if you need one removed.",
        val editModeRequired: String = "Enable edit mode with /mia lootcrates edit to change loot crates",
        val editModeEnabled: String = "Loot crate edit mode enabled",
        val editModeDisabled: String = "Loot crate edit mode disabled",
        val notLookingAtCrate: String = "You are not looking at a loot crate",
        val customLootSaved: String = "Saved custom loot",
        val customLootNotSaved: String = "The crate was removed while editing, its items were given back to you",
        val lootNotLoaded: String = "Your looted crates could not be loaded, rejoin or let an administrator know!",
        val resetForEveryone: String = "Reset this crate for everyone",
        val resetForPlayer: String = "Reset this crate for %s",
        val lootTableItemTitle: String = "<green>Loot Crate: <gold>%s<yellow>:%s",
    )
}

interface LootCrates {
    val cache: LootedCache

    fun tableKeys(): List<PrefabKey>
    fun table(key: PrefabKey): LootTable?
    fun settings(key: PrefabKey): LootCrateSettings

    fun furnitureIds(): Set<String>
    fun sounds(furnitureId: String): CrateSounds
    fun crateItem(table: PrefabKey): ItemStack?
    fun missingItems(table: PrefabKey): List<String>
    fun roll(loot: ContainsLoot, crate: ItemDisplay?): List<ItemStack?>?

    fun closeViewers(crate: UUID, players: Collection<Player> = Bukkit.getOnlinePlayers())
    fun openLoot(player: Player, loot: ContainsLoot, crate: ItemDisplay?, contents: List<ItemStack?>, looting: LootInventory.Looting?)
    fun preview(player: Player, loot: ContainsLoot, crate: ItemDisplay?)
}

private fun Duration.formatted() = inWholeSeconds.seconds.toString()

/** Taken from the table's name suffix, like tools-rare */
val PrefabKey.rarity: ItemRarity
    get() = when (key.substringAfterLast('-')) {
        "uncommon" -> ItemRarity.UNCOMMON
        "rare" -> ItemRarity.RARE
        "endgame" -> ItemRarity.EPIC
        else -> ItemRarity.COMMON
    }

val LootCratesFeature = module("lootcrates") {
    require(get<AbyssFeatureConfig>().lootCrates.enabled) { "Lootcrates feature is disabled" }
    requirePlugins("Nexo")

    val config by singleConfig<LootCratesConfig>("lootTables.yml")
    single { config.messages }
    Idofront.setupDataStore(LootCratesDataStore)

    val impl = object : LootCrates {
        private val geary get() = gearyPaper.worldManager.global

        override val cache = LootedCache()

        override fun tableKeys(): List<PrefabKey> = with(geary) {
            queryManager.getEntitiesMatchingAsSequence(family {
                hasSet<LootTable>()
                hasSet<PrefabKey>()
            }).mapNotNull { it.toGeary().get<PrefabKey>() }.toList()
        } + LootCrateConstants.CUSTOM_LOOT_TABLE

        override fun table(key: PrefabKey): LootTable? =
            if (key == LootCrateConstants.CUSTOM_LOOT_TABLE) LootTable.empty()
            else with(geary) { key.toEntityOrNull()?.get<LootTable>() }

        override fun settings(key: PrefabKey): LootCrateSettings =
            with(geary) { key.toEntityOrNull()?.get<LootCrateSettings>() } ?: LootCrateSettings.DEFAULT

        override fun furnitureIds(): Set<String> =
            tableKeys().mapTo(mutableSetOf()) { settings(it).furnitureId } + LootCrateSettings.DEFAULT.furnitureId

        override fun sounds(furnitureId: String): CrateSounds = with(geary) {
            queryManager
                .getEntitiesMatchingAsSequence(family {
                    hasSet<CrateSounds>()
                    hasSet<PrefabKey>()
                })
                .map { it.toGeary() }
                .firstOrNull { it.get<PrefabKey>()?.nexoId == furnitureId }
                ?.get<CrateSounds>()
        } ?: CrateSounds.DEFAULT

        override fun crateItem(table: PrefabKey): ItemStack? {
            val settings = settings(table)
            val item = NexoItems.itemFromId(settings.furnitureId)?.build() ?: return null
            val title = config.messages.lootTableItemTitle.format(table.namespace, table.key).miniMsg()
            val lore = listOf(
                "<gray>Furniture: <white>${settings.furnitureId}",
                "<gray>Restock: <white>${settings.restock?.formatted() ?: "never"}",
                "",
                "<yellow>Left-click <gray>to take",
                "<yellow>Right-click <gray>to preview loot",
            ).map { "<!i>$it".miniMsg() }

            return item.apply {
                with(geary) { editPersistentDataContainer { it.encode(ContainsLoot(table)) } }
                setData(DataComponentTypes.ITEM_NAME, title)
                setData(DataComponentTypes.RARITY, table.rarity)
                setData(DataComponentTypes.LORE, ItemLore.lore(lore))
            }
        }

        override fun missingItems(table: PrefabKey): List<String> {
            fun missing(entry: LootEntry): List<String> = when (entry) {
                is LootEntry.Group -> entry.children.flatMap(::missing)
                is LootEntry.Item -> listOfNotNull(entry.name.takeIf { Material.matchMaterial(it) == null })
                is LootEntry.IdofrontItem -> listOfNotNull(entry.item.prefab?.takeUnless(::itemExists))
            }

            return table(table)
                ?.pools
                ?.flatMap { pool -> pool.entries.flatMap(::missing) }
                ?.distinct()
                .orEmpty()
        }

        private fun itemExists(prefab: String): Boolean {
            val provider = prefab.substringBefore(' ', missingDelimiterValue = "")
            val id = prefab.substringAfter(' ')

            return when (provider) {
                "nexo" -> NexoItems.exists(id)
                "" -> runCatching { with(geary) { PrefabKey.of(id).toEntityOrNull() } }.getOrNull() != null
                else -> true
            }
        }

        override fun roll(loot: ContainsLoot, crate: ItemDisplay?): List<ItemStack?>? {
            if (loot.isCustomLoot()) return crate?.customLoot.orEmpty()

            val table = table(loot.table) ?: return null
            return Bukkit.createInventory(null, 27).apply(table::populateInventory).contents.toList()
        }

        override fun openLoot(player: Player, loot: ContainsLoot, crate: ItemDisplay?, contents: List<ItemStack?>, looting: LootInventory.Looting?) {
            val furnitureId = crate?.let { NexoFurniture.furnitureMechanic(it)?.itemID } ?: settings(loot.table).furnitureId
            val sounds = sounds(furnitureId)
            val title = table(loot.table)?.itemName ?: Component.text("Loot")
            val lootInventory = LootInventory(crate, sounds, title, looting).inventory.apply {
                contents.take(size).forEachIndexed(::setItem)
            }

            crate?.playCrateSound(sounds.openSound) ?: player.playSound(sounds.openSound)
            player.openInventory(lootInventory)
        }

        override fun closeViewers(crate: UUID, players: Collection<Player>) {
            players
                .filter { (it.openInventory.topInventory.getHolder(false) as? LootInventory)?.looting?.crateId == crate }
                .forEach { it.closeInventory() }
        }

        override fun preview(player: Player, loot: ContainsLoot, crate: ItemDisplay?) {
            val contents = roll(loot, crate) ?: return player.error(config.messages.tableNotFound.format(loot.table))
            openLoot(player, loot, crate, contents, null)
        }
    }

    val editMode = LootCrateEditMode(impl)
    val overrides = CrateOverrides(impl)
    val furnitureSync = CrateFurnitureSync(impl)

    single<LootCrates> { impl }
    single { impl.cache }
    single { editMode }
    single { overrides }

    Bukkit.getWorlds()
        .flatMap { it.getEntitiesByClass(ItemDisplay::class.java) }
        .forEach(ItemDisplay::restoreContainsLoot)

    // Loot tables are prefabs, which finish loading a while after features do
    launch {
        PrefabLoading.awaitLoaded().await()
        overrides.cover(impl.furnitureIds())
        furnitureSync.syncLoaded()

        impl.tableKeys().forEach { table ->
            val missing = impl.missingItems(table)
            if (missing.isNotEmpty()) abyss.logger.w("Loot table $table references items that do not exist: ${missing.joinToString()}")
        }
    }

    launch {
        while (true) {
            delay(10.ticks)
            editMode.showTargetedTables(Bukkit.getOnlinePlayers())
        }
    }

    val listener = new(::LootCratesListener)
    listeners(listener, new(::LootCrateEditingListener), furnitureSync)
    Bukkit.getOnlinePlayers().forEach(listener::load)

    addCloseable {
        // Closeables run in reverse, so the listeners are still registered to save these
        Bukkit.getOnlinePlayers()
            .filter { it.openInventory.topInventory.getHolder(false).let { holder -> holder is LootInventory || holder is CustomLootEditor } }
            .forEach { it.closeInventory() }
        overrides.close()
        listener.close()
    }
}.mainCommand {
    "lootcrates" {
        fun lootTableArg() = GearyArgs.prefabKey().oneOf { get<LootCrates>().tableKeys() }

        fun Player.targetCrate(msg: LootCratesConfig.Messages): ItemDisplay? =
            NexoFurniture.findTargetFurniture(this)
                ?.takeIf { it.containsLoot != null }
                ?: run {
                    error(msg.notLookingAtCrate)
                    null
                }

        "menu" {
            description = "Browse every loot table and take its crate"
            executes.asPlayer {
                if (!player.hasPermission(LootCratePermissions.EDIT))
                    return@asPlayer player.error(get<LootCratesConfig.Messages>().noPermissionToEdit)

                val lootCrates = get<LootCrates>()
                guiy(player) { LootCrateMenu(player, lootCrates) }
            }
        }

        "edit" {
            description = "Toggle edit mode, where crates can be placed and broken and opening one previews it"
            executes.asPlayer {
                val msg = get<LootCratesConfig.Messages>()
                if (!player.hasPermission(LootCratePermissions.EDIT)) return@asPlayer player.error(msg.noPermissionToEdit)

                val enabled = get<LootCrateEditMode>().toggle(player)
                player.success(if (enabled) msg.editModeEnabled else msg.editModeDisabled)
            }
        }

        "preview" {
            description = "Roll a loot table without looting anything"
            executes.asPlayer().args("table" to lootTableArg()) { table ->
                get<LootCrates>().preview(player, ContainsLoot(table), null)
            }
        }

        "inspect" {
            description = "Show information about the crate you are looking at"
            executes.asPlayer {
                val crate = player.targetCrate(get<LootCratesConfig.Messages>()) ?: return@asPlayer
                val loot = crate.containsLoot ?: return@asPlayer
                val lootCrates = get<LootCrates>()
                val settings = lootCrates.settings(loot.table)
                val openedCount = player.readBlocking { LootCratesDataStore.countLooted(crate.uniqueId) }
                val yourLoot = lootCrates.cache.current(player, crate.uniqueId, settings.restock)
                val missing = lootCrates.missingItems(loot.table)

                val location = crate.location
                val tableState = if (lootCrates.table(loot.table) == null) " <red>(missing)" else ""
                val lootState = when {
                    yourLoot == null -> "not rolled"
                    yourLoot.leftovers == null -> "emptied"
                    else -> "${yourLoot.leftovers.decodeLeftovers().count { it != null }} stacks left"
                }
                val rolledAgo = yourLoot?.let { ", rolled ${(Clock.System.now() - it.rolledAt).formatted()} ago" }.orEmpty()

                player.info("<gold>Loot crate at <yellow>${location.blockX} ${location.blockY} ${location.blockZ}".miniMsg())
                player.info("<gray>Table: <white>${loot.table}$tableState".miniMsg())
                player.info("<gray>Furniture: <white>${settings.furnitureId}".miniMsg())
                player.info("<gray>Restock: <white>${settings.restock?.formatted() ?: "never"}".miniMsg())
                if (missing.isNotEmpty()) player.info("<red>Missing items: ${missing.joinToString()}".miniMsg())
                player.info("<gray>Opened by: <white>$openedCount players".miniMsg())
                player.info("<gray>Your loot: <white>$lootState$rolledAgo".miniMsg())
            }
        }

        "migrate" {
            description = "Replace the old chest crates listed in a file under plugins/MineInAbyss with furniture crates"
            requires { sender.isOp }

            fun IdoCommandContext.migrate(file: String, dryRun: Boolean) {
                val migration = ChestCrateMigration(get(), get())
                val targets = runCatching { migration.parse(abyss.dataPath.resolve(file)) }
                    .getOrElse { return sender.error("Could not read $file: ${it.message}") }

                sender.info("${if (dryRun) "Checking" else "Migrating"} ${targets.size} crates from $file")

                abyss.launch {
                    val report = migration.run(targets, dryRun)
                    report.failed.forEach { (target, reason) -> abyss.logger.w("Loot crate migration failed at $target: $reason") }

                    val summary = buildString {
                        append("${if (dryRun) "Can migrate" else "Migrated"} ${report.migrated.size}, ")
                        append("already migrated ${report.alreadyMigrated.size}, failed ${report.failed.size}")
                        if (report.failed.isNotEmpty()) append(", see the console for failures")
                    }
                    sender.success(summary)
                }
            }

            "check" {
                executes.args("file" to Args.greedyString()) { file -> migrate(file, dryRun = true) }
            }

            "run" {
                executes.args("file" to Args.greedyString()) { file -> migrate(file, dryRun = false) }
            }
        }

        "reset" {
            description = "Let players loot the crate you are looking at again"

            "all" {
                executes.asPlayer {
                    val msg = get<LootCratesConfig.Messages>()
                    val crate = player.targetCrate(msg) ?: return@asPlayer

                    val lootCrates = get<LootCrates>()
                    lootCrates.closeViewers(crate.uniqueId)
                    lootCrates.cache.forgetAll(crate.uniqueId)
                    player.launchWrite { LootCratesDataStore.deleteCrate(crate.uniqueId) }
                    NexoFurniture.refreshFurnitureItem(crate)
                    player.success(msg.resetForEveryone)
                }
            }

            "player" {
                executes.asPlayer().args("player" to Args.offlinePlayer()) { target ->
                    val msg = get<LootCratesConfig.Messages>()
                    val crate = player.targetCrate(msg) ?: return@args

                    val lootCrates = get<LootCrates>()
                    lootCrates.closeViewers(crate.uniqueId, listOfNotNull(target.player))
                    lootCrates.cache.forget(target.uniqueId, crate.uniqueId)
                    player.launchWrite { LootCratesDataStore.delete(target.uniqueId.toKotlinUuid(), crate.uniqueId) }
                    target.player?.let(crate::resendTo)
                    player.success(msg.resetForPlayer.format(target.name))
                }
            }
        }
    }
}

fun Args.prefabKey() = ArgsMinecraft.key()
    .map { PrefabKey.of(it.namespace(), it.value()) }
