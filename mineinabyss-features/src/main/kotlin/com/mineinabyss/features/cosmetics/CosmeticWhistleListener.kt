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

class CosmeticWhistleListener : Listener {

    @EventHandler
    fun CosmeticTypeRegisterEvent.equipWhistleCosmetic() {
        config.node("slot-parent").string?.takeIf { it == "MIA_BACKPACK" }?.let { MiaCosmeticBackpackType(id, config) }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerCosmeticRemoveEvent.onRemoveBackpack() {
        if (cosmetic.slot == CosmeticSlot.BACKPACK)
            abyss.plugin.launch {
                yield()
                user.equipWhistleCosmetic()
            }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerJoinEvent.onJoinWithoutCosmetics() {
        abyss.plugin.launch {
            yield()
            player.cosmeticUser?.equipWhistleCosmetic()
        }
    }

}