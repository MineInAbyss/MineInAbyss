package com.mineinabyss.features.cosmetics

import com.hibiscusmc.hmccosmetics.api.events.PlayerCosmeticPostEquipEvent
import com.mineinabyss.deeperworld.event.PlayerChangeSectionEvent
import com.mineinabyss.features.helpers.cosmeticUser
import com.mineinabyss.features.helpers.layerWhistleCosmetic
import me.lojosho.hibiscuscommons.util.packets.PacketManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.EquipmentSlot

class CosmeticWhistleListener : Listener {

    @EventHandler
    fun PlayerCosmeticPostEquipEvent.equipWhistleCosmetic() {
        user.player?.equipWhistleCosmetic()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerChangeSectionEvent.onAscendOrDescend() {
        player.equipWhistleCosmetic()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerJoinEvent.onJoin() {
        player.equipWhistleCosmetic()
    }

    private fun Player.equipWhistleCosmetic() {
        val cosmeticUser = cosmeticUser ?: return
        val outsideViewers = cosmeticUser.userBackpackManager?.entityManager?.refreshViewers(location)?.plus(this) ?: return
        val layerWhistle = layerWhistleCosmetic() ?: return
        PacketManager.equipmentSlotUpdate(cosmeticUser.userBackpackManager.firstArmorStandId, EquipmentSlot.HAND, layerWhistle, outsideViewers)
    }
}