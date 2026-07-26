package com.mineinabyss.features.npc.shopkeeping.menu

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.components.canvases.MAX_CHEST_HEIGHT
import com.mineinabyss.idofront.textcomponents.miniMsg
import me.dvyy.compose.mini.layout.jetpack.Row
import me.dvyy.compose.mini.layout.modifiers.height
import me.dvyy.compose.mini.layout.modifiers.offset
import me.dvyy.compose.mini.layout.modifiers.size
import me.dvyy.compose.mini.layout.modifiers.width
import me.dvyy.compose.mini.modifier.Modifier

@Composable
fun ShopUIScope.ShopHomeScreen(
    onNavigateToSpecial: () -> Unit,
    onNavigateToBuyScreen: () -> Unit,
    onNavigateToSellScreen: () -> Unit,
) = Chest(shopKeeper.menu, Modifier.height(6.dp)) {
    Row(Modifier.offset(1.dp, 1.dp)) {
        OpenSellMenu(onNavigateToSellScreen = onNavigateToSellScreen)
        Spacer(Modifier.width(1.dp))
        OpenBuyMenu(onNavigateToBuyScreen = onNavigateToBuyScreen)
    }
    OpenSpecialMenu(Modifier.offset(3.dp, (MAX_CHEST_HEIGHT - 2).dp), onNavigateToSpecial)
    CloseButton(Modifier.offset(0.dp, (MAX_CHEST_HEIGHT - 1).dp))
}

@Composable
fun ShopUIScope.OpenBuyMenu(modifier: Modifier = Modifier, onNavigateToBuyScreen: () -> Unit) {
    Button(
        modifier = modifier,
        enabled = shopKeeper.buying.isNotEmpty(),
        onClick = onNavigateToBuyScreen,
    ) { enabled ->
        if (enabled) Text(
            "<gold><b>Sought after Wares".miniMsg(),
            modifier = modifier.size(3.dp, 2.dp)
        ) else Text(
            "<gold><b><st>Sought after Wares".miniMsg(),
            "<red>${shopKeeper.name} is not looking for any wares.".miniMsg(),
            modifier = modifier.size(3.dp, 2.dp)
        )
    }
}

@Composable
fun ShopUIScope.OpenSellMenu(
    modifier: Modifier = Modifier,
    onNavigateToSellScreen: () -> Unit,
) {
    Button(
        modifier = modifier,
        enabled = shopKeeper.selling.isNotEmpty(),
        onClick = onNavigateToSellScreen,
    ) { enabled ->
        if (enabled) Text(
            "<gold><b>Wares for Sale".miniMsg(),
            modifier = modifier.size(3.dp, 2.dp)
        ) else Text(
            "<gold><b><st>Wares for Sale".miniMsg(),
            "<red>${shopKeeper.name} has no wares for sale.".miniMsg(),
            modifier = modifier.size(3.dp, 2.dp)
        )
    }
}

@Composable
fun ShopUIScope.OpenSpecialMenu(
    modifier: Modifier = Modifier,
    onNavigateToSpecial: () -> Unit,
) {
    Button(
        modifier = modifier,
        enabled = shopKeeper.specialTrades.isNotEmpty(),
        onClick = onNavigateToSpecial,
    ) { enabled ->
        if (enabled) Text(
            "<gold><b>Special Trade Offers".miniMsg(),
            modifier = modifier.size(3.dp, 2.dp)
        ) else Text(
            "<gold><b><st>Special Trade Offers".miniMsg(),
            "<red>${shopKeeper.name} has no special trades.".miniMsg(),
            modifier = modifier.size(3.dp, 2.dp)
        )
    }
}
