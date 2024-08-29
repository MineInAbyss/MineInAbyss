package com.mineinabyss.features.patreons

import com.mineinabyss.chatty.components.ChattyNickname
import com.mineinabyss.components.players.Patreon
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.luckPerms
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.datastore.remove
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.nms.nbt.editOfflinePDC
import net.luckperms.api.context.ContextSet
import net.luckperms.api.context.ImmutableContextSet
import net.luckperms.api.model.user.User
import net.luckperms.api.node.NodeType
import net.luckperms.api.track.Track
import org.bukkit.OfflinePlayer

fun OfflinePlayer.removePatreonPerks() {
    val patreonTrack = luckPerms.trackManager.getTrack("patreon") ?: return abyss.logger.e("Failed to get patreon-track")
    luckPerms.userManager.modifyUser(uniqueId) { user ->
        patreonTrack.recursiveDemote(user, ImmutableContextSet.empty())
        user.data().clear(NodeType.PREFIX.predicate())
    }
    if (isOnline) {
        val gearyPlayer = player!!.toGeary()
        gearyPlayer.remove<ChattyNickname>()
        val patreon = gearyPlayer.get<Patreon>() ?: return
        gearyPlayer.setPersisting(patreon.copy(tier = 0))
    } else editOfflinePDC {
        this.remove<ChattyNickname>()
        val patreon = this.decode<Patreon>() ?: return
        encode(patreon.copy(tier = 0))
    }
    abyss.logger.s("Set Patreon-tier to 0 & removed nickname")
}

private fun Track.recursiveDemote(user: User, contextSet: ContextSet) {
    this.demote(user, contextSet).also { if (it.groupTo.isPresent) this.recursiveDemote(user, contextSet) }
}