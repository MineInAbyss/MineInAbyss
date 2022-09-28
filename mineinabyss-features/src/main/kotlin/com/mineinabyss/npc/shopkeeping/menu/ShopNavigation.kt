package com.mineinabyss.npc.shopkeeping.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mineinabyss.components.npc.shopkeeping.ShopKeeper
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.navigation.Navigator
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.messaging.miniMsg
import org.bukkit.entity.Player

sealed class ShopScreen(val title: String, val height: Int) {
    class Default(shopKeeper: ShopKeeper) : ShopScreen(shopKeeper.menu, 6)
    class Sell(shopKeeper: ShopKeeper) : ShopScreen(shopKeeper.menu, 6)
    class Buy(shopKeeper: ShopKeeper) : ShopScreen(shopKeeper.menu, 6)
    class Special(shopKeeper: ShopKeeper) : ShopScreen(shopKeeper.menu, 6)
}

typealias ShopNav = Navigator<ShopScreen>

class ShopUIScope(
    val player: Player,
    val owner: GuiyOwner,
    val shopKeeper: ShopKeeper
) {
    val nav = ShopNav { ShopScreen.Default(shopKeeper) }
}

@Composable
fun ShopUIScope.BackButton(modifier: Modifier = Modifier) {
    Button(onClick = { nav.back() }, modifier = modifier) {
        Text("<red><b>Back".miniMsg())
    }
}

@Composable
fun ShopUIScope.CloseButton(modifier: Modifier = Modifier) {
    Button(onClick = { player.closeInventory() }, modifier = modifier) {
        Text("<red><b>Close".miniMsg())
    }
}

@Composable
fun NextPageButton(modifier: Modifier = Modifier) {
    Button(onClick = {  }, modifier = modifier) {
        Text("<yellow><b>Next Page".miniMsg())
    }
}
@Composable
fun PreviousPageButton(modifier: Modifier = Modifier) {
    Button(onClick = {  }, modifier = modifier) {
        Text("<yellow><b>Previous Page".miniMsg())
    }
}

@Composable
fun GuiyOwner.ShopMainMenu(player: Player, shopKeeper: ShopKeeper) {
    val scope = remember { ShopUIScope(player, this, shopKeeper) }
    scope.apply {
        nav.withScreen(setOf(player), onEmpty = ::exit) { screen ->
            Chest(
                setOf(player),
                screen.title,
                Modifier.height(screen.height),
                onClose = { player.closeInventory() }
            ) {
                when (screen) {
                    is ShopScreen.Default -> ShopHomeScreen()
                    is ShopScreen.Sell -> ShopSellMenu()
                    is ShopScreen.Buy -> ShopBuyMenu()
                    is ShopScreen.Special -> ShopSpecialMenu()
                }
            }
        }
    }
}
