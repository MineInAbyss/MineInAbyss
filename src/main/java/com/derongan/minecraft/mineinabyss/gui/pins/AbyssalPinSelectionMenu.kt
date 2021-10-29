package com.derongan.minecraft.mineinabyss.gui.pins

import androidx.compose.runtime.Composable
import com.derongan.minecraft.mineinabyss.ecs.components.pins.AbyssalPin
import com.derongan.minecraft.mineinabyss.ecs.components.pins.ActivePins
import com.mineinabyss.geary.ecs.engine.iteration.QueryResult
import com.mineinabyss.geary.ecs.prefab.PrefabKey
import com.mineinabyss.geary.ecs.query.Query
import com.mineinabyss.geary.minecraft.access.toGeary
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import org.bukkit.entity.Player

object AbyssalPinsQuery : Query() {
    val QueryResult.prefab by get<PrefabKey>()
    val QueryResult.pin by get<AbyssalPin>()
}

@Composable
fun GuiyOwner.PinSelectionMenu(player: Player) {
    Chest(listOf(player), title = "Select a Pin", onClose = { exit() }) {
        val gearyPlayer = player.toGeary()
        val activePins = gearyPlayer.get<ActivePins>() ?: return@Chest

        //TODO efficiently get all prefabs with AbyssalPin components
        Grid(9, 6) {
            AbyssalPinsQuery.apply {
                asSequence()
                    .map { it.prefab }
                    .filter { it !in activePins }
                    .shuffled()
                    .take(3)
                    .forEach { key ->
                        Pin(key, Modifier.clickable {
                            activePins += key
                            this@PinSelectionMenu.exit()
                        })
                    }
            }
        }
    }
}
