package com.mineinabyss.features.npc.shopkeeping.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mineinabyss.components.npc.shopkeeping.ShopKeeper
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.inventory.LocalGuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.navigation.NavHost
import com.mineinabyss.guiy.navigation.composable
import com.mineinabyss.guiy.navigation.rememberNavController
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.entity.Player

sealed class ShopScreen(val title: String, val height: Int) {
    class Default(shopKeeper: ShopKeeper) : ShopScreen(shopKeeper.menu, 6)
    class Sell(shopKeeper: ShopKeeper) : ShopScreen(shopKeeper.menu, 6)
    class Buy(shopKeeper: ShopKeeper) : ShopScreen(shopKeeper.menu, 6)
    class Special(shopKeeper: ShopKeeper) : ShopScreen(shopKeeper.menu, 6)
}

class ShopUIScope(
    val player: Player,
    val owner: GuiyOwner,
    val shopKeeper: ShopKeeper,
) {
}

// TODO navigation
@Composable
fun ShopUIScope.BackButton(modifier: Modifier = Modifier) {
    Button(onClick = { /*nav.back()*/ }, modifier = modifier) {
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
    val nav = rememberNavController()
    scope.apply {
        // TODO move to composables
//        Chest(
//            screen.title,
//            Modifier.height(screen.height),
//            onClose = { player.closeInventory() }
//        ) {
//        }
        NavHost(nav, startDestination = ShopScreen.Default(shopKeeper)) {
            composable<ShopScreen.Default> { ShopHomeScreen() }
            composable<ShopScreen.Sell> { ShopHomeScreen() }
            composable<ShopScreen.Buy> { ShopHomeScreen() }
            composable<ShopScreen.Special> { ShopHomeScreen() }
        }
    }
}
