package com.mineinabyss.features.dialogs

import com.mineinabyss.idofront.util.substringBetween
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.event.player.PlayerCustomClickEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class DialogListener : Listener {

    @EventHandler
    fun PlayerCustomClickEvent.onMarkerToggle() {
        val tag = tag ?: return
        if (identifier != DialogHelpers.dialogId) return

        val habo = tag.string().substringBetween("habo:", ",") == "1b"
        val okibo = tag.string().substringBetween("okibo:", ",") == "1b"
        val pogjaw = tag.string().substringBetween("pogjaw:", "}") == "1b"
        MapDialog(habo, okibo, pogjaw).showDialog((commonConnection as PlayerGameConnection).player)
    }

    @EventHandler
    fun PlayerCustomClickEvent.onOkiboLineClick() {
        val tag = tag ?: return
        val player = (commonConnection as? PlayerGameConnection)?.player ?: return

        if (identifier == DialogHelpers.okiboMarker) {
            val current = tag.string().substringBetween("current", ",")
            val destination = tag.string().substringBetween("destination", "}")
            OkiboLineConfirmationDialog(current, destination).showDialog(player)
        } else if (identifier == DialogHelpers.okiboMarkerConfirm) {

            if (tag != null) {
                val current = tag.string().substringBetween("current:", ",")
                val destination = tag.string().substringBetween("destination:", "}")

                player.closeDialog()
                player.sendMessage("<gold> Going from $current to $destination RN on an Okibo :)")
            } else player.closeDialog()

        }

    }
}


