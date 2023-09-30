package com.mineinabyss.features.core

import com.mineinabyss.components.core.SignOwner
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.idofront.messaging.error
import org.bukkit.block.Sign
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent

class PreventSignEditListener : Listener {

    private val BYPASS_PERMISSION = "mineinabyss.preventsignedit.bypass"

    @EventHandler
    fun PlayerInteractEvent.onPlayerEditSign() {
        val sign = clickedBlock?.state as? Sign ?: return
        val signOwner = sign.persistentDataContainer.decode<SignOwner>()
        if (player.hasPermission(BYPASS_PERMISSION)) return

        // Make people without bypass perm not be able to edit signs that don't have the component
        when {
            signOwner == null -> setUseInteractedBlock(Event.Result.DENY)
            signOwner.owner != player.uniqueId -> setUseInteractedBlock(Event.Result.DENY)
            else -> return
        }

        player.error("You do not have permission to edit this sign.")
    }

    @EventHandler
    fun BlockPlaceEvent.onPlaceSign() {
        (block.state as? Sign)?.let {
            it.persistentDataContainer.encode(SignOwner(player.uniqueId))
            it.update()
        }
    }
}
