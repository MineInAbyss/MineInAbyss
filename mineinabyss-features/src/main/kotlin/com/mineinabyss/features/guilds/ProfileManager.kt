package com.mineinabyss.features.guilds

import com.destroystokyo.paper.profile.PlayerProfile
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import java.util.*

object ProfileManager {
    val profileCache: Object2ObjectOpenHashMap<UUID, PlayerProfile> = Object2ObjectOpenHashMap()
    val profileUpdateChannel = Channel<ProfileRequest>(Channel.UNLIMITED)

    suspend fun getOrRequestProfile(uuid: UUID): PlayerProfile {
        profileCache[uuid]?.let { return it }

        val deferred = CompletableDeferred<PlayerProfile>()
        profileUpdateChannel.send(ProfileRequest(uuid, deferred))
        return deferred.await()
    }

}

data class ProfileRequest(
    val uuid: UUID,
    val result: CompletableDeferred<PlayerProfile>
)
