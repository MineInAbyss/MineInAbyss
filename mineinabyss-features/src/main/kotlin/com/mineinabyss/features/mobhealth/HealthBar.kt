package com.mineinabyss.features.mobhealth

import com.mineinabyss.features.abyss
import com.ticxo.modelengine.api.ModelEngineAPI
import com.ticxo.modelengine.api.model.ActiveModel
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes
import com.ticxo.modelengine.api.model.bone.ModelBone
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.plugin.Plugin
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.roundToInt

class HealthBar(
    private val plugin: Plugin,
    private val glyphs: BarGlyphs,
    private val visibleTicks: Int,
    private val mob: LivingEntity,
) {
    private val head = mob.headBone()
    private val aboveModel = mob.visualHeight() + MARGIN

    // Bone locations are pivots, usually the neck, so the bar is lifted to clear the model top as measured at spawn and then follows the head
    private val headLift = head?.let { (mob.location.y + aboveModel - it.location.y).coerceAtLeast(MIN_HEAD_LIFT) }

    private val scale = ((mob.visualWidth() * WIDTH_SHARE).coerceIn(WIDTH_RANGE) / (glyphs.linePixels * BLOCKS_PER_PIXEL)).toFloat()
    private val frame = spawnLayer(0)
    private val trailLayer = spawnLayer(1)
    private val fillLayer = spawnLayer(2)
    private val layers = listOf(frame, trailLayer, fillLayer)

    private val viewers = mutableMapOf<Player, Int>()
    private var age = 0
    private var lastHit = 0
    private var fill = glyphs.steps
    private var trail = glyphs.steps
    private var trailFrom = glyphs.steps

    init {
        frame.text(glyphs.frame)
        render()
    }

    fun hit(player: Player, fraction: Double) {
        if (viewers.put(player, age) == null) layers.forEach { player.showEntity(plugin, it) }
        trailFrom = trail
        fill = (fraction.coerceIn(0.0, 1.0) * glyphs.steps).roundToInt()
        lastHit = age
        render()
    }

    /** @return false once no player can see the bar anymore */
    fun tick(): Boolean {
        age++
        if (!mob.isDead) anchor().let { at -> layers.forEach { it.teleport(at) } }
        updateTrail()
        viewers.entries.removeIf { (player, hitAt) ->
            (age - hitAt > visibleTicks || !player.isOnline).also { expired ->
                if (expired) layers.forEach { player.hideEntity(plugin, it) }
            }
        }
        return viewers.isNotEmpty()
    }

    fun remove() = layers.forEach { it.remove() }

    private fun updateTrail() {
        val sinceHit = age - lastHit
        if (trail == fill || sinceHit < TRAIL_DELAY) return
        val progress = ((sinceHit - TRAIL_DELAY + 1).toFloat() / TRAIL_TICKS).coerceAtMost(1f)
        trail = (trailFrom + (fill - trailFrom) * progress).roundToInt()
        render()
    }

    private fun render() {
        trailLayer.text(glyphs.trail(trail))
        fillLayer.text(glyphs.fill(fill))
    }

    private fun anchor(): Location = head?.location?.add(0.0, headLift ?: 0.0, 0.0)
        ?: mob.location.add(0.0, aboveModel, 0.0)

    private fun spawnLayer(index: Int) = mob.world.spawn(anchor(), TextDisplay::class.java) {
        it.backgroundColor = Color.fromARGB(0)
        it.billboard = Display.Billboard.CENTER
        it.brightness = Display.Brightness(15, 15)
        it.isPersistent = false
        it.isVisibleByDefault = false
        it.teleportDuration = 2
        it.transformation = Transformation(Vector3f(0f, 0f, index * LAYER_GAP), Quaternionf(), Vector3f(scale), Quaternionf())
    }

    private companion object {
        const val MARGIN = 0.1
        const val MIN_HEAD_LIFT = 0.25
        const val WIDTH_SHARE = 0.35
        val WIDTH_RANGE = 0.4..1.5
        const val LAYER_GAP = 0.003f
        const val TRAIL_DELAY = 8
        const val TRAIL_TICKS = 8

        // A TextDisplay draws one font pixel as 1/40 of a block
        const val BLOCKS_PER_PIXEL = 0.025
    }
}

private fun LivingEntity.models(): Collection<ActiveModel> =
    if (abyss.isModelEngineEnabled) ModelEngineAPI.getModeledEntity(this)?.models?.values.orEmpty() else emptyList()

// Head bones pass their behaviour on to child bones, so the outermost one is the head itself
private fun LivingEntity.headBone(): ModelBone? = models().flatMap { it.bones.values }.firstOrNull {
    it.hasBoneBehavior(BoneBehaviorTypes.HEAD) && it.parent?.hasBoneBehavior(BoneBehaviorTypes.HEAD) != true
}

// ModelEngine mobs are often far larger than their vanilla hitbox, so the bar is sized and placed against the model instead
private fun LivingEntity.visualHeight() = models().maxOfOrNull { it.blueprint.mainHitbox.height * it.scale.y() } ?: height

private fun LivingEntity.visualWidth() = models().maxOfOrNull { it.blueprint.mainHitbox.width * it.scale.x() } ?: width
