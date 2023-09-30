package com.mineinabyss.features.okibotravel

import androidx.compose.runtime.Composable
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.*
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.entity.Player

@Composable
fun GuiyOwner.OkiboTravelMenu(player: Player, feature: OkiboTravelFeature) {
    Chest(setOf(player), ":something:", Modifier.height(4), onClose = { player.closeInventory() }) {
        feature.travelPoints.forEachIndexed { index, travelPoint ->
            TravelPointButtons(player, modifier = Modifier.at(index, 1), travelPoint)
        }
    }
}

@Composable
fun TravelPointButtons(player: Player, modifier: Modifier, travelPoint: OkiboTravelPoint) {
    Item(
        TitleItem.of(travelPoint.name.miniMsg()).editItemMeta { setCustomModelData(0) },
        modifier.size(1,1).clickable {
            player.teleport(travelPoint.travelPoint)
            player.success("Fast travelled to ${travelPoint.name}")
        }
    )
}
