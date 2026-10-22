package com.mineinabyss.features.cinematics

import com.mineinabyss.features.helpers.viewThrough
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import net.kyori.adventure.key.Key
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Player
import kotlin.time.Duration

internal const val PLAYER_RIDER = "@player"

internal class SceneFailure(message: String) : Exception(message)

internal class CinematicSession(
    val player: Player,
    val key: PrefabKey,
    val scene: CinematicScene,
    val anchor: CinematicAnchor,
    val environment: Map<String, Any?>,
    val origin: Location,
    /** What the player rides, a seat where they started until they board a gondola */
    var seat: ArmorStand,
) {
    lateinit var job: Job
    var completed = false
    var restored = false
    var skipRequested = false
    /** Set once the screen is faded out, from then on the rest of the scene plays out without waiting */
    var skipping = false
    // Set while the player is moved from one seat to another, so the dismount is not read as a skip
    var relocating = false
    var camera: ArmorStand? = null
    var viewing: Entity? = null
    val mannequins = mutableMapOf<String, Mannequin>()
    val riders = mutableMapOf<String, GondolaRider>()
    val gondolas = mutableMapOf<String, CinematicGondola>()
    val effects = mutableSetOf<Key>()
    val chunkTickets = mutableSetOf<Chunk>()
    val carried: GondolaRider? get() = riders[PLAYER_RIDER]

    suspend fun tick() {
        if (!skipping) delay(1.ticks)
    }

    suspend fun pause(duration: Duration) {
        var left = duration.tickCount()
        while (left-- > 0 && !skipping) delay(1.ticks)
    }

    suspend fun view(entity: Entity) {
        if (viewing === entity) return
        viewing = entity
        if (entity.ticksLived < TRACKER_TICKS) delay((TRACKER_TICKS - entity.ticksLived).ticks)
        if (viewing !== entity || restored) return
        player.viewThrough(entity)
    }

    fun remove(id: String) {
        riders.remove(id)?.let { rider ->
            rider.gondola.removeRider(rider.riser)
            rider.riser.remove()
        }
        val mannequin = mannequins.remove(id)
        if (mannequin != null) mannequin.remove() else gondolas.remove(id)?.remove()
    }
}
