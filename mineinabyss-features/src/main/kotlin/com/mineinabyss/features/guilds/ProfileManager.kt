package com.mineinabyss.features.guilds

import com.destroystokyo.paper.profile.PlayerProfile
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.features.abyss
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withTimeoutOrNull
import org.bukkit.Bukkit
import java.util.*

object ProfileManager {
    val profileCache: Object2ObjectOpenHashMap<UUID, PlayerProfile> = Object2ObjectOpenHashMap()
    val profileUpdateChannel = Channel<ProfileRequest>(Channel.UNLIMITED)
    private val activeProfileJobs = mutableMapOf<UUID, CompletableDeferred<PlayerProfile>>()
    private var status = false

    suspend fun getOrRequestProfile(uuid: UUID): PlayerProfile {
        profileCache[uuid]?.let { return it }

        // Check if a task is already in progress for the UUID
        activeProfileJobs[uuid]?.let { return it.await() }

        // Create a new deferred task for the UUID
        val deferred = CompletableDeferred<PlayerProfile>()
        activeProfileJobs[uuid] = deferred

        profileUpdateChannel.send(ProfileRequest(uuid, deferred))
        return deferred.await()
    }

    // Coroutine that processes all profile requests
    fun startProfileFetching() {
        if (status) return
        status = true
        abyss.plugin.launch(abyss.plugin.asyncDispatcher) {
            while (status) {
                val requests = mutableListOf<ProfileRequest>()

                repeat(10) {
                    val request = withTimeoutOrNull(50) { profileUpdateChannel.receive() }
                    request?.let { requests.add(it) }
                }

                requests.forEach { request ->
                    runCatching {
                        val profile = fetchProfileForUUID(request.uuid)
                        profileCache[request.uuid] = profile
                        request.result.complete(profile)
                        activeProfileJobs.remove(request.uuid)
                    }.onFailure {
                        request.result.completeExceptionally(it)
                        activeProfileJobs.remove(request.uuid)

                    }
                }

                delay(100L)  // Add delay to control request rate
            }
        }
    }

    fun stopProfileFetching() {
        status = false
        activeProfileJobs.values.forEach { it.cancel() }
        activeProfileJobs.clear()
    }

    private suspend fun fetchProfileForUUID(uuid: UUID): PlayerProfile {
        return Bukkit.getOfflinePlayer(uuid).playerProfile.update().await()
    }
}


data class ProfileRequest(
    val uuid: UUID,
    val result: CompletableDeferred<PlayerProfile>
)
