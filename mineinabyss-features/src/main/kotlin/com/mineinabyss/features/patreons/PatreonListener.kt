package com.mineinabyss.features.patreons

import com.mineinabyss.components.players.Patreon
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.api.API
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.nms.nbt.editOfflinePDC
import github.scarsz.discordsrv.api.Subscribe
import github.scarsz.discordsrv.api.events.AccountLinkedEvent
import github.scarsz.discordsrv.api.events.AccountUnlinkedEvent
import github.scarsz.discordsrv.util.DiscordUtil
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PatreonListener(private val config: PatreonFeature.Config) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerJoinEvent.addPatreonComponent() {
        val member = API.DiscordSRV?.plugin?.run { DiscordUtil.getMemberById(accountLinkManager.getDiscordId(player.uniqueId) ?: "") }
        if (member == null) player.removePatreonPerks()
        else {
            val roleIds = member.roles.map { it.id }
            val tier = config.patreonRoles.values.filter { it.roleId in roleIds }.maxOfOrNull { it.patreonTier } ?: 0
            val gearyPlayer = player.toGeary()
            val patreon = gearyPlayer.get<Patreon>() ?: Patreon()
            gearyPlayer.setPersisting(patreon.copy(tier = tier))
        }
    }

    @Subscribe
    fun AccountLinkedEvent.onLinkDiscord() {
        val member = DiscordUtil.getMemberById(user.id) ?: return
        val roleIds = member.roles.map { it.id }
        val tier = config.patreonRoles.values.filter { it.roleId in roleIds }.maxOfOrNull { it.patreonTier } ?: 0

        if (player.isOnline) {
            val gearyPlayer = player.player!!.toGeary()
            val patreon = gearyPlayer.get<Patreon>() ?: Patreon()
            gearyPlayer.setPersisting(patreon.copy(tier = tier))
        } else player.editOfflinePDC {
            with(abyss.gearyGlobal) {
                val patreon = decode<Patreon>() ?: Patreon()
                encode(patreon.copy(tier = tier))
            }
        }
    }

    @Subscribe
    fun AccountUnlinkedEvent.onUnlinkDiscord() {
        player.removePatreonPerks()
    }
}
