package com.mineinabyss.features.cosmetics

import com.github.shynixn.mccoroutine.bukkit.launch
import com.hibiscusmc.hmccosmetics.api.events.CosmeticTypeRegisterEvent
import com.hibiscusmc.hmccosmetics.api.events.PlayerCosmeticRemoveEvent
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.cosmeticUser
import com.mineinabyss.features.helpers.equipWhistleCosmetic
import kotlinx.coroutines.yield
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class CosmeticWhistleListener(private val cosmeticConfig: CosmeticsFeature.Config) : Listener {

    @EventHandler
    fun CosmeticTypeRegisterEvent.equipWhistleCosmetic() {
        config.node("slot-parent").string?.takeIf { it == "MIA_BACKPACK" } ?: return
        MiaCosmeticBackpackType(id, config, cosmeticConfig.equipWhistleCosmetic)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerCosmeticRemoveEvent.onRemoveBackpack() {
        if (!cosmeticConfig.equipWhistleCosmetic || cosmetic.slot != CosmeticSlot.BACKPACK) return
        abyss.plugin.launch {
            yield()
            user.equipWhistleCosmetic()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerJoinEvent.onJoinWithoutCosmetics() {
        if (!cosmeticConfig.equipWhistleCosmetic) return
        abyss.plugin.launch {
            yield()
            player.cosmeticUser?.equipWhistleCosmetic()
        }
    }

}