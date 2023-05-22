package com.mineinabyss.features.okibotravel.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mineinabyss.components.okibotravel.OkiboLineStation
import com.mineinabyss.components.okibotravel.OkiboTraveler
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.features.okibotravel.OkiboTravelFeature
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.navigation.Navigator
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.entity.Player

sealed class OkiboScreen(var title: String, val height: Int) {
    object Default : OkiboScreen(":something:", 3)

    class OkiboTravelConfirmationScreen(val startStation: OkiboLineStation, val destination: OkiboLineStation) :
        OkiboScreen(":something:", 3)
}

@Composable
fun GuiyOwner.OkiboMainScreen(player: Player, feature: OkiboTravelFeature, okiboTraveler: OkiboTraveler) {
    val scope = remember { OkiboUIScope(player, feature) }
    scope.apply {
        nav.withScreen(setOf(player), onEmpty = ::exit) { screen ->
            Chest(setOf(player), screen.title, Modifier.height(screen.height), onClose = { player.closeInventory() }) {
                when (screen) {
                    is OkiboScreen.Default -> OkiboTravelMenu(player, feature, okiboTraveler)
                    is OkiboScreen.OkiboTravelConfirmationScreen ->
                        OkiboTravelConfirmationMenu(player, feature, screen.startStation, screen.destination)
                }
            }
        }
    }
}

typealias OkiboNav = Navigator<OkiboScreen>

class OkiboUIScope(
    val player: Player,
    val feature: OkiboTravelFeature
) {
    val nav = OkiboNav { OkiboScreen.Default }
}

@Composable
fun OkiboUIScope.BackButton(modifier: Modifier = Modifier) {
    Button(onClick = { nav.back() }, modifier = modifier) {
        Text("<red><b>Back".miniMsg())
    }
}
