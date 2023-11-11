package com.mineinabyss.features.helpers

import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey

object CoinFactory {
    val orthCoin get() = gearyItems.createItem(PrefabKey.of("mineinabyss", "orthcoin"))
    val mittyToken get() = gearyItems.createItem(PrefabKey.of("mineinabyss", "patreon_token"))
}
