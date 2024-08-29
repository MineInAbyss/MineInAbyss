package com.mineinabyss.features.patreons

import com.mineinabyss.chatty.components.ChattyNickname
import com.mineinabyss.chatty.components.chattyNickname
import com.mineinabyss.components.players.Patreon
import com.mineinabyss.features.abyss
import com.mineinabyss.features.abyssFeatures
import com.mineinabyss.features.helpers.luckPerms
import com.mineinabyss.features.helpers.luckpermGroups
import com.mineinabyss.features.playerprofile.DiscordButton
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.datastore.remove
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.getOrSetPersisting
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.nms.nbt.editOfflinePDC
import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.api.Subscribe
import github.scarsz.discordsrv.api.events.AccountLinkedEvent
import github.scarsz.discordsrv.api.events.AccountUnlinkedEvent
import github.scarsz.discordsrv.util.DiscordUtil
import net.luckperms.api.context.ContextSet
import net.luckperms.api.context.DefaultContextKeys
import net.luckperms.api.context.ImmutableContextSet
import net.luckperms.api.node.types.PermissionNode
import net.luckperms.api.node.types.PrefixNode
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PatreonListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerJoinEvent.addPatreonComponent() {
        val member = DiscordUtil.getMemberById(DiscordSRV.getPlugin().accountLinkManager.getDiscordId(player.uniqueId) ?: "")
        if (member == null) player.removePatreonPerks()
        else {
            val roleIds = member.roles.map { it.id }
            val tier = abyssFeatures.patreon.patreonRoles.values.filter { it.roleId in roleIds }.maxOfOrNull { it.patreonTier } ?: 0
            val gearyPlayer = player.toGeary()
            val patreon = gearyPlayer.get<Patreon>() ?: return
            gearyPlayer.setPersisting(patreon.copy(tier = tier))
        }
    }

    @Subscribe
    fun AccountLinkedEvent.onLinkDiscord() {
        val member = DiscordUtil.getMemberById(user.id) ?: return
        val roleIds = member.roles.map { it.id }
        val tier = abyssFeatures.patreon.patreonRoles.values.filter { it.roleId in roleIds }.maxOfOrNull { it.patreonTier } ?: 0

        if (player.isOnline) {
            val gearyPlayer = player.player!!.toGeary()
            val patreon = gearyPlayer.get<Patreon>() ?: Patreon()
            gearyPlayer.setPersisting(patreon.copy(tier = tier))
        }
        else player.editOfflinePDC {
            val patreon = this.decode<Patreon>() ?: Patreon()
            encode(patreon.copy(tier = tier))
        }
    }

    @Subscribe
    fun AccountUnlinkedEvent.onUnlinkDiscord() {
        player.removePatreonPerks()
    }
}
