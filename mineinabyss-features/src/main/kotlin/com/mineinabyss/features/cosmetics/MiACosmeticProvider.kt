package com.mineinabyss.features.cosmetics

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticProvider
import com.mineinabyss.features.abyss
import org.bukkit.plugin.Plugin

class MiACosmeticProvider : CosmeticProvider() {
    override fun getProviderPlugin(): Plugin? {
        return abyss.plugin
    }
}