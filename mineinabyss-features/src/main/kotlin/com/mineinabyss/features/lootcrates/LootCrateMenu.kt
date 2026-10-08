package com.mineinabyss.features.lootcrates

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mineinabyss.components.lootcrates.ContainsLoot
import com.mineinabyss.guiy.canvas.LocalGuiyOwner
import com.mineinabyss.guiy.components.HorizontalGrid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.modifiers.click.clickable
import me.dvyy.compose.mini.layout.modifiers.size
import me.dvyy.compose.mini.modifier.Modifier
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

@Composable
fun LootCrateMenu(player: Player, lootCrates: LootCrates) {
    val owner = LocalGuiyOwner.current
    val tables = lootCrates.tableKeys().sortedWith(
        compareBy({ it.key.substringBeforeLast('-') }, { it.rarity.ordinal })
    )

    Chest(title = "Loot Crates", onClose = { owner.exit() }) {
        HorizontalGrid(Modifier.size(9.dp, 6.dp)) {
            tables.forEach { table ->
                Item(lootCrates.crateItem(table), Modifier.clickable {
                    when (clickType) {
                        ClickType.RIGHT -> lootCrates.preview(player, ContainsLoot(table), null)
                        else -> lootCrates.crateItem(table)?.let(player.inventory::addItem)
                    }
                })
            }
        }
    }
}
