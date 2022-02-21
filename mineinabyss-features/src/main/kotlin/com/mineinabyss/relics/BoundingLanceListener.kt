package com.mineinabyss.relics

import com.mineinabyss.components.relics.BoundingLance
import com.mineinabyss.geary.papermc.spawnFromPrefab
import com.mineinabyss.geary.papermc.toPrefabKey
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.util.toMCKey
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BoundingLanceListener : Listener {

    @EventHandler
    fun PlayerInteractEvent.spawnBoundingLance() {
        if (hand != EquipmentSlot.HAND) return
        if (!rightClicked) return
        if (clickedBlock == null) return
        if (blockFace == BlockFace.DOWN) return
        val item = player.inventory.itemInMainHand
        val gearyItem = item.toGearyOrNull(player) ?: return
        val boundingLance = gearyItem.get<BoundingLance>() ?: return
        val loc = player.getLastTwoTargetBlocks(null, 6)

        val newLoc =
            if (blockFace == BlockFace.UP)
                loc.last()?.location?.toCenterLocation()?.apply { y += 0.5 } ?: return
            else
                loc.first()?.location?.toCenterLocation()?.apply { y -= 0.5 } ?: return

        val prefab = "mineinabyss:boundinglance".toMCKey().toPrefabKey()

        newLoc.spawnFromPrefab(prefab)
        newLoc.broadcastVal()
        player.playSound(newLoc, boundingLance.placeSound, 1f, 1f)
    }
}