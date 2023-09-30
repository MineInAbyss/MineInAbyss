package com.mineinabyss.features.pins.ui

import androidx.compose.runtime.Composable
import com.mineinabyss.components.pins.ActivePins
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.GearyItemProvider
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.size
import org.bukkit.entity.Player

@Composable
fun GuiyOwner.PinMenu(player: Player) {
    Chest(setOf(player), title = "Active Pins", onClose = { exit() }) {
        val gearyPlayer = player.toGeary()
        val pins = gearyPlayer.get<ActivePins>() ?: return@Chest
        Grid(Modifier.size(9, 6)) {
            pins.forEach { Pin(it) }
        }
    }
}


@Composable
fun Pin(key: PrefabKey, modifier: Modifier = Modifier) {
    val item = GearyItemProvider().serializePrefabToItemStack(key) ?: return
    Item(item, modifier)
}
