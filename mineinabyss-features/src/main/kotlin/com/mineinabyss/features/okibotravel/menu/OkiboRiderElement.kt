package com.mineinabyss.features.okibotravel.menu

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.features.abyss
import com.mineinabyss.features.okibotravel.OkiboModelConfig
import com.mineinabyss.idofront.time.ticks
import com.mineinabyss.features.overlay.PlaneElement
import com.mineinabyss.features.overlay.PlaneLayout
import com.mineinabyss.features.overlay.PlaneView
import com.ticxo.modelengine.api.ModelEngineAPI
import com.ticxo.modelengine.api.entity.Dummy
import com.ticxo.modelengine.api.model.ActiveModel
import com.ticxo.modelengine.api.model.ModeledEntity
import com.ticxo.modelengine.api.utils.OffsetMode
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

private const val RIDER_DROP = 0.6
private const val TURN = 15.0
private const val FILL = 0.8
private const val DEPTH = 2.7
private const val FOOTING = 0.08

class OkiboRiderElement private constructor(
    override val x: Double,
    override val y: Double,
    override val w: Double,
    override val h: Double,
    val okibo: OkiboModelConfig,
) : PlaneElement {
    override val label: String? = null
    override val onClick: (() -> Unit)? = null
    override val key = "okibo_rider:$x:$y:$w:$h"
    private var rig: Rig? = null

    companion object {
        fun of(x: Double, y: Double, w: Double, h: Double, okibo: OkiboModelConfig) = OkiboRiderElement(x, y, w, h, okibo)
            .takeIf { abyss.isModelEngineEnabled && runCatching { ModelEngineAPI.getBlueprint(okibo.blueprint) }.getOrNull() != null }
    }

    private fun origin(view: PlaneView): Location {
        val forward = view.eye.direction.setY(0).normalize()
        val turn = Math.toRadians(TURN)
        return view.at(x + w / 2, y + h - FOOTING, DEPTH).apply {
            direction = forward.clone().crossProduct(Vector(0, 1, 0)).multiply(cos(turn)).subtract(forward.multiply(sin(turn)))
            pitch = 0f
        }
    }

    private fun scale(view: PlaneView): Double {
        val k = view.fit * DEPTH / PlaneLayout.PLANE_DISTANCE
        val turn = Math.toRadians(TURN)
        val across = okibo.length * cos(turn) + okibo.width * sin(turn)
        return minOf(FILL * w * PlaneLayout.pageWidthBlocks * k / across, h * PlaneLayout.pageHeightBlocks * k / okibo.riderTop)
    }

    override fun spawn(player: Player, view: PlaneView) {
        rig = runCatching { Rig.spawn(player, okibo, origin(view), scale(view)) }
            .onFailure { abyss.logger.w("Okibo rider failed to spawn: $it") }
            .getOrNull()
    }

    override fun adopt(old: PlaneElement, view: PlaneView): Boolean {
        val previous = old as? OkiboRiderElement ?: return false
        rig = previous.rig?.takeIf { it.valid }?.also { it.place(origin(view)) } ?: return false
        previous.rig = null
        return true
    }

    override fun hover(on: Boolean) {}

    override fun remove() {
        rig?.remove()
        rig = null
    }

    private class Rig(
        val okibo: OkiboModelConfig,
        val dummy: Dummy<Unit>,
        val modeled: ModeledEntity,
        val model: ActiveModel,
        val seat: ArmorStand,
        val rider: Mannequin,
        val backpack: ArmorStand?,
    ) {
        private var facing = dummy.yBodyRot
        private val follower = abyss.launch {
            while (true) {
                delay(1.ticks)
                follow()
            }
        }
        val valid get() = !modeled.isDestroyed && seat.isValid && rider.isValid

        fun place(root: Location) {
            facing = root.yaw
            dummy.syncLocation(root)
        }

        // ModelEngine animates off the main thread, getLocation reads its last finished frame
        private fun follow() {
            val body = model.getBone(okibo.bodyBone).orElse(null)?.takeIf { modeled.isInitialized } ?: return
            val at = body.getLocation(OffsetMode.LOCAL, Vector3f(0f, okibo.saddle.toFloat(), 0f), true).apply {
                world = seat.world
                yaw = facing
                pitch = 0f
            }
            seat.teleport(at)
        }

        fun remove() {
            follower.cancel()
            runCatching { modeled.destroy() }
            dummy.isRemoved = true
            listOfNotNull(backpack, rider, seat).forEach(Entity::remove)
        }

        companion object {
            fun spawn(player: Player, okibo: OkiboModelConfig, root: Location, scale: Double): Rig {
                val model = ModelEngineAPI.createActiveModel(okibo.blueprint).apply {
                    setScale(scale)
                    setCanHurt(false)
                    bones.values.forEach { it.blockLight = 15; it.skyLight = 15 }
                }
                val dummy = Dummy<Unit>().apply {
                    // Culling goes by the player's own look, which is steering the cursor
                    isDetectingPlayers = false
                    syncLocation(root)
                    setForceViewing(player, true)
                    data.setCullExempt(player.uniqueId, true)
                }
                val modeled = ModelEngineAPI.createModeledEntity(dummy).apply { addModel(model, false) }
                val spawned = mutableListOf<Entity>()
                return runCatching {
                    model.animationHandler.playAnimation(okibo.walkAnimation, 0.0, 0.0, 1.0, true)
                    val seatAt = root.clone().add(0.0, (okibo.bodyPivot + okibo.saddle) * scale, 0.0)
                    val seat = Figures.markerStand(player, seatAt, 1.0).also(spawned::add)
                    val rider = Figures.mannequin(player, player, seatAt.clone().subtract(0.0, RIDER_DROP * scale, 0.0), scale)
                        .also { spawned += it; seat.addPassenger(it) }
                    val backpack = Figures.backpack(player)?.let { item ->
                        Figures.markerStand(player, rider.location.add(0.0, 1.8 * scale, 0.0), scale).also {
                            spawned += it
                            it.equipment.setHelmet(item)
                            rider.addPassenger(it)
                        }
                    }
                    Rig(okibo, dummy, modeled, model, seat, rider, backpack)
                }.onFailure {
                    runCatching { modeled.destroy() }
                    dummy.isRemoved = true
                    spawned.forEach(Entity::remove)
                }.getOrThrow()
            }
        }
    }
}
