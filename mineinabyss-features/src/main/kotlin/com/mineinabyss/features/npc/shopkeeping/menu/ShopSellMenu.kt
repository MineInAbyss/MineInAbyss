package com.mineinabyss.features.npc.shopkeeping.menu

import androidx.compose.runtime.Composable
import com.mineinabyss.features.npc.shopkeeping.handleTrades
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.canvases.CHEST_WIDTH
import com.mineinabyss.guiy.components.canvases.MAX_CHEST_HEIGHT
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size

@Composable
fun ShopUIScope.ShopSellMenu() {
    ShopKeeperSellingTrades(Modifier.at(3, 0))
    BackButton(Modifier.at(0, MAX_CHEST_HEIGHT - 1))
    NextPageButton(Modifier.at(CHEST_WIDTH-1, MAX_CHEST_HEIGHT - 1))
    PreviousPageButton(Modifier.at(CHEST_WIDTH-3, MAX_CHEST_HEIGHT - 1))
}

@Composable
fun ShopUIScope.ShopKeeperSellingTrades(modifier: Modifier) {
    Grid(modifier.size(5, 6)) {
        shopKeeper.selling.handleTrades(player)
    }
}
