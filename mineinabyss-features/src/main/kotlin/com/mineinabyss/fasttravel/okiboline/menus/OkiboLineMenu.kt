package com.mineinabyss.fasttravel.okiboline.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import org.bukkit.entity.Player

//TODO Consider using fontmagic-stuff from Combimagnetron to make a dynamic map
// Means awful FPS but since this is exclusive to menus it should be fine
// Needs to be tested on subpar machines tho, as on a high-end PC the FPS-loss is massive but still acceptable
// 3060ti, 5800x, 32gb RAM and 450 -> 45 fps

@Composable
fun GuiyOwner.OkiboLineScreen(player: Player) {

    Chest(
        setOf(player),
        title = "Okibo Line",
        Modifier.height(4),
        onClose = { player.closeInventory() }) {

        }
}
