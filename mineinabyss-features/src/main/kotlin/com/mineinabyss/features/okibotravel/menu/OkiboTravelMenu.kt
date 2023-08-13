package com.mineinabyss.features.okibotravel.menu

import androidx.compose.runtime.Composable
import com.mineinabyss.components.okibotravel.OkiboLineStation
import com.mineinabyss.components.okibotravel.OkiboTraveler
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.features.okibotravel.okiboLine
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun OkiboUIScope.OkiboTravelMenu(player: Player, okiboTraveler: OkiboTraveler) {
    okiboLine.config.okiboStations.forEachIndexed { index, station ->
        //TODO modifier.at to place in grid based on index
        TravelPointButton(player, Modifier.size(1,1).at(index), station, okiboTraveler)
    }
}

@Composable
fun TravelPointButton(player: Player, modifier: Modifier, station: OkiboLineStation, okiboTraveler: OkiboTraveler) {
    //TODO
    // Make button for station
    // Cost = okiboTraveler.mainStation
    val cost = okiboTraveler.costTo(station, okiboLine.config.okiboStations) ?: return
    Button(onClick = { player.openInventory.title = cost.toString() }) {
        Item(ItemStack(Material.STONE))
    }
}
