package com.mineinabyss.features.npc.shopkeeping.menu

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mineinabyss.features.npc.shopkeeping.handleTrades
import com.mineinabyss.guiy.components.HorizontalGrid
import com.mineinabyss.guiy.components.canvases.CHEST_WIDTH
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.components.canvases.MAX_CHEST_HEIGHT
import me.dvyy.compose.mini.layout.modifiers.height
import me.dvyy.compose.mini.layout.modifiers.offset
import me.dvyy.compose.mini.layout.modifiers.size
import me.dvyy.compose.mini.modifier.Modifier

@Composable
fun ShopUIScope.ShopSellMenu() = Chest(shopKeeper.menu, Modifier.height(6.dp)) {
    ShopKeeperSellingTrades(Modifier.offset(3.dp, 0.dp))
    BackButton(Modifier.offset(0.dp, (MAX_CHEST_HEIGHT - 1).dp))
    NextPageButton(Modifier.offset((CHEST_WIDTH - 1).dp, (MAX_CHEST_HEIGHT - 1).dp))
    PreviousPageButton(Modifier.offset((CHEST_WIDTH - 3).dp, (MAX_CHEST_HEIGHT - 1).dp))
}

@Composable
fun ShopUIScope.ShopKeeperSellingTrades(modifier: Modifier) {
    HorizontalGrid(modifier.size(5.dp, 6.dp)) {
        shopKeeper.selling.handleTrades(player)
    }
}
