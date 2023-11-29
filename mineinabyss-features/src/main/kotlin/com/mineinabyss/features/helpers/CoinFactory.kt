package com.mineinabyss.features.helpers

import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey

object CoinFactory {
    val orthCoin get() = gearyItems.createItem(PrefabKey.of("mineinabyss", "orth_coin"))
    val mittyToken get() = gearyItems.createItem(PrefabKey.of("mineinabyss", "mitty_token"))
}
