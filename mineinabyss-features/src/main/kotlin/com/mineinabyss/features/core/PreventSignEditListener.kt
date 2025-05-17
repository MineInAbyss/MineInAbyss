package com.mineinabyss.features.core

import com.mineinabyss.components.core.SignOwner
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.idofront.messaging.error
import org.bukkit.block.Sign
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent

class PreventSignEditListener : Listener {

    companion object {
        private const val BYPASS_PERMISSION = "mineinabyss.preventsignedit.bypass"
    }

    @EventHandler
    fun PlayerInteractEvent.onPlayerEditSign() {
        val signOwner = (clickedBlock?.state as? Sign)?.withGeary { it.persistentDataContainer.decode<SignOwner>() } ?: return
        if (signOwner.owner == player.uniqueId || player.hasPermission(BYPASS_PERMISSION)) return

        // Make people without bypass perm not be able to edit signs that don't have the component
        setUseInteractedBlock(Event.Result.DENY)
        player.error("You do not have permission to edit this sign.")
    }

    @EventHandler
    fun BlockPlaceEvent.onPlaceSign() {
        (block.state as? Sign)?.withGeary { sign ->
            sign.persistentDataContainer.encode(SignOwner(player.uniqueId))
            sign.update()
        }
    }
}
