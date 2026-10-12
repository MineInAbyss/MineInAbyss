package com.mineinabyss.features.cinematics

import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.ticxo.modelengine.api.ModelEngineAPI
import com.ticxo.modelengine.api.entity.Dummy
import com.ticxo.modelengine.api.model.ActiveModel
import com.ticxo.modelengine.api.model.render.DisplayRenderer
import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player

// ModelEngine sends its pivot this far below where the bones hang, they ride on top of it
private const val PIVOT_DROP = 0.5
// Ticks the client smooths each carrier move over, enough to hide a late packet without trailing far behind
private const val CARRIER_SMOOTHING = 2
private const val ANIMATION_LERP = 0.25

/**
 * A ModelEngine model with no entity behind it, only shown to [viewer].
 *
 * The client moves an entity carrying the local player without interpolation whenever that entity gets a full position sync, which is how ModelEngine moves its pivot.
 * So the model and its riders sit still server side, and ride an item display on the viewer's client instead, which is moved a little every tick and smoothed
 */
internal class CinematicGondola private constructor(
    private val dummy: Dummy<Any>,
    private val model: ActiveModel,
    private val carrier: ItemDisplay,
    private val viewer: Player,
    start: Location,
) {
    private val modeled = ModelEngineAPI.createModeledEntity(dummy).also { it.addModel(model, false) }
    private val pivot get() = (model.modelRenderer as? DisplayRenderer)?.pivot
    private val riders = mutableListOf<Entity>()
    private var current: Location = start.clone()

    /** Where the model is shown, the server side entities stay behind until the next [jumpTo] */
    val location: Location get() = current.clone()

    private var measuredMountHeight: Double? = null

    /**
     * How far above [location] ModelEngine puts the pivot, the model's eye height.
     * Measured once, since the pivot only follows [jumpTo] on ModelEngine's next update
     */
    private val mountHeight: Double
        get() = measuredMountHeight
            ?: pivot?.position?.get()?.y?.let { it - dummy.location.y }?.also { measuredMountHeight = it }
            ?: 0.0

    /** Where the carrier sits for the model to show at [location], riders mounted on it start here too */
    val riderBase: Location get() = location.add(0.0, mountHeight - PIVOT_DROP, 0.0)

    fun moveTo(location: Location) {
        current = location.clone()
        val base = riderBase
        carrier.toNMS().setPos(base.x, base.y, base.z)
    }

    /** Moves the server side entities along too, for jumps the carrier cannot be smoothed over such as into another section */
    fun jumpTo(location: Location) {
        val height = mountHeight
        current = location.clone()
        dummy.syncLocation(location)
        carrier.teleportDuration = 0
        carrier.teleport(location.clone().add(0.0, height - PIVOT_DROP, 0.0))
    }

    fun play(animation: String) {
        model.animationHandler.playAnimation(animation, ANIMATION_LERP, ANIMATION_LERP, 1.0, true)
    }

    fun addRider(entity: Entity) {
        riders += entity
    }

    fun removeRider(entity: Entity) {
        riders -= entity
    }

    /**
     * Mounts the pivot and riders on the carrier for the viewer.
     * The client must already know all of them, and drops this again whenever it drops the carrier
     */
    fun link() {
        carrier.teleportDuration = CARRIER_SMOOTHING
        val pivotId = pivot?.id ?: return abyss.logger.w("Gondola model has no display pivot to mount")
        val buf = FriendlyByteBuf(Unpooled.buffer())
        buf.writeVarInt(carrier.entityId)
        buf.writeVarIntArray(intArrayOf(pivotId) + riders.filter { it.isValid }.map { it.entityId })
        viewer.toNMS().connection.send(ClientboundSetPassengersPacket.STREAM_CODEC.decode(buf))
    }

    fun hideFromViewer() = dummy.setForceViewing(viewer, false)

    fun showToViewer() = dummy.setForceViewing(viewer, true)

    fun remove() {
        modeled.destroy()
        dummy.isRemoved = true
        carrier.remove()
    }

    companion object {
        fun spawn(model: String, location: Location, viewer: Player): CinematicGondola? {
            val blueprint = ModelEngineAPI.getBlueprint(model) ?: return null
            val active = ModelEngineAPI.createActiveModel(blueprint)
            val dummy = Dummy<Any>().apply {
                isDetectingPlayers = false
                syncLocation(location)
                setForceViewing(viewer, true)
            }
            val carrier = location.world.spawn(location.clone().subtract(0.0, PIVOT_DROP, 0.0), ItemDisplay::class.java) { display ->
                display.isPersistent = false
                display.isVisibleByDefault = false
                display.teleportDuration = CARRIER_SMOOTHING
            }
            viewer.showEntity(abyss, carrier)
            return CinematicGondola(dummy, active, carrier, viewer, location)
        }
    }
}
