package com.mineinabyss.okiboline.menus

import androidx.compose.runtime.*
import com.mineinabyss.components.fasttravel.okiboline.okiboLine
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.height
import org.bukkit.entity.Player

//TODO Consider using fontmagic-stuff from Combimagnetron to make a dynamic map
// Means awful FPS but since this is exclusive to menus it should be fine
// Needs to be tested on subpar machines tho, as on a high-end PC the FPS-loss is massive but still acceptable
// 3060ti, 5800x, 32gb RAM and 450 -> 45 fps

/*
https://cdn.discordapp.net/attachments/758785982005903431/1002695422780899418/unknown.png
https://cdn.discordapp.net/attachments/758785982005903431/1002696130817167430/unknown.png
https://cdn.discordapp.net/attachments/758785982005903431/1002695462043783178/unknown.png
*/


//TODO Reuse the page stuff from Guild Lookup List to move between points

@Composable
fun GuiyOwner.OkiboLineMenu(player: Player) {
    val playGif = player.okiboLine.useGifForMenuTransition
    val currentPoint = player.okiboLine.currentPoint
    var pageNum by remember { mutableStateOf(currentPoint) }
    Chest(
        setOf(player),
        title = "Okibo Line",
        Modifier.height(6),
        onClose = { player.closeInventory() }) {

        PreviousOkiboPointButton(Modifier.at(0,3), pageNum) {
            pageNum--
        }
        NextOkiboPointButton(Modifier.at(7, 3), pageNum) {
            pageNum++
        }
        /*Row(Modifier.at(0, 2)) {
            PreviousOkiboPointButton()
            Spacer(width = 8)
            NextOkiboPointButton()
        }*/
    }
}

@Composable
fun PreviousOkiboPointButton(modifier: Modifier = Modifier, currentPoint: Int, onClick: () -> Unit) {

}

@Composable
fun NextOkiboPointButton(modifier: Modifier = Modifier, currentPoint: Int, onClick: () -> Unit) {

}

/*@Composable
fun PreviousOkiboPointButton(playGifForPointTransition: Boolean) {
    if (playGifForPointTransition) {
        //TODO
    } else {
        //TODO
    }
}

@Composable
fun NextOkiboPointButton(playGifForPointTransition: Boolean) {
    if (playGifForPointTransition) {
        //TODO
    } else {
        //TODO
    }
}*/
