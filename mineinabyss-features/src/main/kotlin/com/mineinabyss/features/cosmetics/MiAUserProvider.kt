package com.mineinabyss.features.cosmetics

import com.hibiscusmc.hmccosmetics.user.CosmeticUser
import com.hibiscusmc.hmccosmetics.user.CosmeticUserProvider
import com.mineinabyss.features.abyss
import org.bukkit.plugin.Plugin
import java.util.*

class MiAUserProvider : CosmeticUserProvider() {
    override fun createCosmeticUser(playerId: UUID): CosmeticUser {
        return MiACosmeticUser(playerId)
    }

    override fun getProviderPlugin(): Plugin? {
        return abyss.plugin
    }
}