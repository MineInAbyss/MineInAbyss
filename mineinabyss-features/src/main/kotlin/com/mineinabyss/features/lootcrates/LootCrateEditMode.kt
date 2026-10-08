package com.mineinabyss.features.lootcrates

import com.mineinabyss.features.lootcrates.constants.LootCratePermissions
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.nexomc.nexo.api.NexoFurniture
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class LootCrateEditMode(private val lootCrates: LootCrates) {
    private val editors = ConcurrentHashMap.newKeySet<UUID>()

    operator fun contains(player: Player) =
        player.uniqueId in editors && player.hasPermission(LootCratePermissions.EDIT)

    fun toggle(player: Player): Boolean = !editors.remove(player.uniqueId) && editors.add(player.uniqueId)

    fun leave(player: Player) {
        editors.remove(player.uniqueId)
    }

    fun showTargetedTables(players: Collection<Player>) {
        players.filter { it in this }.forEach { player ->
            val loot = NexoFurniture.findTargetFurniture(player)?.containsLoot ?: return@forEach
            val missing = if (lootCrates.table(loot.table) == null) " <red>(missing)" else ""

            player.sendActionBar("<gold>Loot crate: <yellow>${loot.table}$missing".miniMsg())
        }
    }
}
