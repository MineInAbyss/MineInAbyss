package com.mineinabyss.features.helpers

import com.mineinabyss.features.abyss
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.PrefabKey

object CoinFactory {
    private val itemTracking get() = abyss.gearyGlobal.getAddon(ItemTracking)
    val orthCoin get() = itemTracking.createItem(PrefabKey.of("mineinabyss", "orth_coin"))
    val mittyToken get() = itemTracking.createItem(PrefabKey.of("mineinabyss", "mitty_token"))
}
