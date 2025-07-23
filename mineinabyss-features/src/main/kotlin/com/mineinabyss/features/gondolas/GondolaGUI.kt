package com.mineinabyss.features.gondolas

import androidx.compose.runtime.Composable
import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.guiy.components.HorizontalGrid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.inventory.LocalGuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.click.clickable
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.entity.Player

@Composable
fun GondolaSelectionMenu(player: Player) {
    val owner = LocalGuiyOwner.current
    val gearyPlayer = player.toGeary()
    val gondolas = gearyPlayer.get<UnlockedGondolas>() ?: return

    Chest(
        setOf(player),
        title = "Choose Spawn Location",
        onClose = { owner.exit() }) {
        HorizontalGrid(Modifier.size(9, 6)) {
            gondolas.keys.forEach {
                GondolaSpawn(player,LoadedGondolas.loaded[it] ?: return@forEach)
            }
        }
    }
}

@Composable
fun GondolaSpawn(player: Player, gondola: Gondola) = Item(
    gondola.displayItem.toItemStack()
        .editItemMeta { itemName(gondola.displayName.miniMsg()) },
    Modifier.clickable {
        player.teleportAsync(gondola.upperLoc)
    }
)
