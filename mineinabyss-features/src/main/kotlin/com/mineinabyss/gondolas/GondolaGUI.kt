package com.mineinabyss.gondolas

import androidx.compose.runtime.Composable
import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.miniMsg
import org.bukkit.entity.Player

@Composable
fun GuiyOwner.GondolaSelectionMenu(player: Player) {
    val gearyPlayer = player.toGeary()
    val gondolas = gearyPlayer.get<UnlockedGondolas>() ?: return

    Chest(setOf(player), title = "Choose Spawn Location", onClose = { exit() }) {
        Grid(Modifier.size(9, 6)) {
            gondolas.keys.forEach { GondolaSpawn(player, LoadedGondolas.loaded[it] ?: return@forEach) }
        }
    }
}

@Composable
fun GondolaSpawn(player: Player, gondola: Gondola) = Item(
    gondola.displayItem.toItemStack().editItemMeta { displayName(gondola.name.miniMsg()) },
    Modifier.clickable {
        player.teleport(gondola.location)
    })
