package com.mineinabyss.pins.ui

import androidx.compose.runtime.Composable
import com.mineinabyss.components.pins.AbyssalPin
import com.mineinabyss.components.pins.ActivePins
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.accessors.get
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.modifiers.size
import org.bukkit.entity.Player

private object AbyssalPinsQuery : GearyQuery() {
    val TargetScope.prefab by get<PrefabKey>()
    val TargetScope.pin by get<AbyssalPin>()
}

@Composable
fun GuiyOwner.AbyssalPinSelectionMenu(player: Player) {
    Chest(setOf(player), title = "Select a Pin", onClose = { exit() }) {
        Row {
        }
        val gearyPlayer = player.toGeary()
        val activePins = gearyPlayer.get<ActivePins>() ?: return@Chest

        //TODO efficiently get all prefabs with AbyssalPin components
        Grid(Modifier.size(9, 6)) {
            AbyssalPinsQuery.apply {
                asSequence()
                    .map { it.prefab }
                    .filter { it !in activePins }
                    .shuffled()
                    .take(3)
                    .forEach { key ->
                        Pin(key, Modifier.clickable {
                            activePins += key
                            this@AbyssalPinSelectionMenu.exit()
                        })
                    }
            }
        }
    }
}
