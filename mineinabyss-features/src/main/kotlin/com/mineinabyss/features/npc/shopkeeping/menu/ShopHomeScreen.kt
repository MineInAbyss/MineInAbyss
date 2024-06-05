package com.mineinabyss.features.npc.shopkeeping.menu

import androidx.compose.runtime.Composable
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.canvases.MAX_CHEST_HEIGHT
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.textcomponents.miniMsg

@Composable
fun ShopUIScope.ShopHomeScreen() {
    Row(Modifier.at(1, 1)) {
        OpenSellMenu()
        Spacer(1)
        OpenBuyMenu()
    }
    OpenSpecialMenu(Modifier.at(3, MAX_CHEST_HEIGHT - 2))
    CloseButton(Modifier.at(0, MAX_CHEST_HEIGHT - 1))
}

@Composable
fun ShopUIScope.OpenBuyMenu(modifier: Modifier = Modifier) {
    Button(
        modifier = modifier,
        enabled = shopKeeper.buying.isNotEmpty(),
        onClick = { nav.open(ShopScreen.Buy(shopKeeper)) },
    ) { enabled ->
        if (enabled) Text(
            "<gold><b>Sought after Wares".miniMsg(),
            modifier = modifier.size(3, 2)
        ) else Text(
            "<gold><b><st>Sought after Wares".miniMsg(),
            "<red>${shopKeeper.name} is not looking for any wares.".miniMsg(),
            modifier = modifier.size(3, 2)
        )
    }
}

@Composable
fun ShopUIScope.OpenSellMenu(modifier: Modifier = Modifier) {
    Button(
        modifier = modifier,
        enabled = shopKeeper.selling.isNotEmpty(),
        onClick = { nav.open(ShopScreen.Sell(shopKeeper)) },
    ) { enabled ->
        if (enabled) Text(
            "<gold><b>Wares for Sale".miniMsg(),
            modifier = modifier.size(3, 2)
        ) else Text(
            "<gold><b><st>Wares for Sale".miniMsg(),
            "<red>${shopKeeper.name} has no wares for sale.".miniMsg(),
            modifier = modifier.size(3, 2)
        )
    }
}

@Composable
fun ShopUIScope.OpenSpecialMenu(modifier: Modifier = Modifier) {
    Button(
        modifier = modifier,
        enabled = shopKeeper.specialTrades.isNotEmpty(),
        onClick = { nav.open(ShopScreen.Special(shopKeeper)) },
    ) { enabled ->
        if (enabled) Text(
            "<gold><b>Special Trade Offers".miniMsg(),
            modifier = modifier.size(3, 2)
        ) else Text(
            "<gold><b><st>Special Trade Offers".miniMsg(),
            "<red>${shopKeeper.name} has no special trades.".miniMsg(),
            modifier = modifier.size(3, 2)
        )
    }
}
