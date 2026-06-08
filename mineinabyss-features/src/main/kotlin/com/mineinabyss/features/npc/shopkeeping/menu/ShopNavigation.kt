package com.mineinabyss.features.npc.shopkeeping.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mineinabyss.components.npc.shopkeeping.ShopKeeper
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.canvas.GuiyOwner
import com.mineinabyss.guiy.canvas.LocalGuiyOwner
import com.mineinabyss.guiy.navigation.LocalBackGestureDispatcher
import com.mineinabyss.guiy.navigation.NavHost
import com.mineinabyss.guiy.navigation.composable
import com.mineinabyss.guiy.navigation.rememberNavController
import com.mineinabyss.idofront.textcomponents.miniMsg
import me.dvyy.compose.mini.modifier.Modifier
import org.bukkit.entity.Player

sealed class ShopScreen() {
    class Default() : ShopScreen()
    class Sell() : ShopScreen()
    class Buy() : ShopScreen()
    class Special() : ShopScreen()
}

class ShopUIScope(
    val player: Player,
    val owner: GuiyOwner,
    val shopKeeper: ShopKeeper,
) {
}

@Composable
fun BackButton(modifier: Modifier = Modifier) {
    val dispatcher = LocalBackGestureDispatcher.current
    Button(onClick = { dispatcher.onBack() }, modifier = modifier) {
        Text("<red><b>Back".miniMsg())
    }
}

@Composable
fun ShopUIScope.CloseButton(modifier: Modifier = Modifier) {
    Button(onClick = { owner.exit() }, modifier = modifier) {
        Text("<red><b>Close".miniMsg())
    }
}

@Composable
fun NextPageButton(modifier: Modifier = Modifier) {
    Button(onClick = { }, modifier = modifier) {
        Text("<yellow><b>Next Page".miniMsg())
    }
}

@Composable
fun PreviousPageButton(modifier: Modifier = Modifier) {
    Button(onClick = { }, modifier = modifier) {
        Text("<yellow><b>Previous Page".miniMsg())
    }
}

@Composable
fun ShopMainMenu(player: Player, shopKeeper: ShopKeeper) {
    val owner = LocalGuiyOwner.current
    val scope = remember { ShopUIScope(player, owner, shopKeeper) }
    scope.apply {
        val nav = rememberNavController()
        NavHost(nav, startDestination = ShopScreen.Default()) {
            composable<ShopScreen.Default> {
                ShopHomeScreen(
                    onNavigateToSpecial = { nav.navigate(ShopScreen.Special()) },
                    onNavigateToBuyScreen = { nav.navigate(ShopScreen.Buy()) },
                    onNavigateToSellScreen = { nav.navigate(ShopScreen.Sell()) },
                )
            }
            composable<ShopScreen.Sell> { ShopSellMenu() }
            composable<ShopScreen.Buy> { ShopBuyMenu() }
            composable<ShopScreen.Special> { ShopSpecialMenu() }
        }
    }
}
