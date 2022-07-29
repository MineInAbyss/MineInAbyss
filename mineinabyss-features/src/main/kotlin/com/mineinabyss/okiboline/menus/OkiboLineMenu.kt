package com.mineinabyss.okiboline.menus

import androidx.compose.runtime.*
import com.mineinabyss.components.fasttravel.okiboline.okiboLine
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.okiboMenus
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.messaging.miniMsg
import org.bukkit.entity.Player

//TODO Consider using fontmagic-stuff from Combimagnetron to make a dynamic map
// Means awful FPS but since this is exclusive to menus it should be fine
// Needs to be tested on subpar machines tho, as on a high-end PC the FPS-loss is massive but still acceptable
// 3060ti, 5800x, 32gb RAM and 450 -> 45 fps

//TODO Reuse the page stuff from Guild Lookup List to move between points

@Composable
fun GuiyOwner.OkiboLineMenu(player: Player) {
    val currentPoint = player.okiboLine.currentPoint
    var pageNum by remember { mutableStateOf(currentPoint) }
    var okiboMenuImage by remember { mutableStateOf(okiboMenus[pageNum]) }

    Chest(
        setOf(player),
        title = okiboMenuImage,
        Modifier.height(6),
        onClose = { player.closeInventory() }) {

        PreviousOkiboPointButton(Modifier.at(0,3), pageNum) {
            pageNum--
            okiboMenuImage = okiboMenus[pageNum]
        }
        NextOkiboPointButton(Modifier.at(7, 3), pageNum) {
            pageNum++
            okiboMenuImage = okiboMenus[pageNum]
        }
    }
}

@Composable
fun PreviousOkiboPointButton(modifier: Modifier = Modifier, currentPoint: Int, onClick: () -> Unit) {
    Button(
        enabled = currentPoint > 1,
        modifier = modifier.at(3, 5),
        onClick = onClick
    ) { Text("<yellow><b>Previous Okibo Point".miniMsg()) }
}

@Composable
fun NextOkiboPointButton(modifier: Modifier = Modifier, currentPoint: Int, onClick: () -> Unit) {
    Button(
        enabled = currentPoint < (okiboMenus.size),
        modifier = modifier.at(5, 5),
        onClick = onClick
    ) { Text("<yellow><b>Next Okibo Point".miniMsg()) }
}
