package com.mineinabyss.features.cosmetics

import com.hibiscusmc.hmccosmetics.api.events.PlayerCosmeticPostEquipEvent
import com.hibiscusmc.hmccosmetics.api.events.PlayerCosmeticRemoveEvent
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.features.helpers.cosmeticUser
import com.mineinabyss.features.helpers.equipWhistleCosmetic
import com.mineinabyss.features.helpers.layer
import io.papermc.paper.event.player.PlayerTrackEntityEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class CosmeticListener(private val equipWhistleCosmetic: Boolean) : Listener {

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        player.cosmeticUser?.updateCosmetic(CosmeticSlot.BACKPACK)
    }

    @EventHandler
    fun PlayerCosmeticRemoveEvent.onUnequip() {
        user.updateCosmetic(CosmeticSlot.BACKPACK)
    }

    @EventHandler
    fun PlayerDescendEvent.onDescend() {
        if (fromSection.layer?.key == toSection.layer?.key) return
        val user = player.cosmeticUser?.takeUnless { it.isHidden } ?: return
        if (equipWhistleCosmetic) user.equipWhistleCosmetic()
    }

    @EventHandler
    fun PlayerAscendEvent.onAscend() {
        if (fromSection.layer?.key == toSection.layer?.key) return
        val user = player.cosmeticUser?.takeUnless { it.isHidden } ?: return
        if (equipWhistleCosmetic) user.equipWhistleCosmetic()
    }

    @EventHandler
    fun PlayerTrackEntityEvent.onTrack() {
        val user = player.cosmeticUser?.takeUnless { it.isHidden } ?: return
        if (equipWhistleCosmetic) user.equipWhistleCosmetic()
        val second = (entity as? Player)?.cosmeticUser?.takeUnless { it.isHidden } ?: return
        if (equipWhistleCosmetic) second.equipWhistleCosmetic()
    }

    @EventHandler
    fun PlayerCosmeticPostEquipEvent.onPostRespawn() {
        user.updateCosmetic(CosmeticSlot.BACKPACK)
    }
}