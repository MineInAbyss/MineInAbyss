package com.mineinabyss.features.overlay

import com.mineinabyss.features.abyss
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Color
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.Vector3f

interface PlaneElement {
    val x: Double
    val y: Double
    val w: Double
    val h: Double
    val label: String?
    val onClick: (() -> Unit)?
    val clickable get() = onClick != null
    val hitBox get() = Box(x, y, w, h)

    val key: String

    fun contains(cx: Float, cy: Float) = hitBox.contains(cx, cy)
    fun spawn(player: Player, view: PlaneView)
    fun adopt(old: PlaneElement, view: PlaneView): Boolean
    fun hover(on: Boolean)
    fun remove()
}

// The interpolation delay is always resent, which makes the client replay its last transformation change.
// The transformation getter hands out a copy, so a full new value is set every time
private fun Display.rescale(scale: Float) {
    val current = transformation.takeUnless { it.scale == Vector3f(scale) } ?: return
    interpolationDelay = 0
    transformation = Transformation(current.translation, current.leftRotation, Vector3f(scale), current.rightRotation)
}

class OverlayElement(
    override val x: Double,
    override val y: Double,
    override val w: Double,
    override val h: Double,
    val text: Component,
    val scale: Float,
    val lineWidth: Int = 200,
    val alignment: TextDisplay.TextAlignment = TextDisplay.TextAlignment.CENTER,
    override val label: String? = null,
    val glyphHeight: Int = 8,
    val ascent: Int = 7,
    val highlighted: Boolean = false,
    val visualW: Double = w,
    val depth: Double = PlaneLayout.PLANE_DISTANCE,
    val background: Color = Color.fromARGB(0, 0, 0, 0),
    hit: Box? = null,
    override val onClick: (() -> Unit)? = null,
) : PlaneElement {
    override val hitBox = hit ?: Box(x, y, w, h)
    override val key = "text:${PlainTextComponentSerializer.plainText().serialize(text)}:$scale:$alignment:$lineWidth:$highlighted:${background.asARGB()}:$label"
    private var display: TextDisplay? = null
    private var view: PlaneView? = null
    private val baseScale get() = (view?.scale(scale) ?: scale) * (if (highlighted) 1.1f else 1f)

    // A glyph's visual bottom sits (2 + ascent - height) px above the display origin
    private fun origin(view: PlaneView) =
        view.at(x + visualW / 2, y + h + (2 + ascent - glyphHeight) * PlaneLayout.TEXT_PIXEL * scale / PlaneLayout.pageHeightBlocks, depth)

    override fun adopt(old: PlaneElement, view: PlaneView): Boolean {
        val previous = old as? OverlayElement ?: return false
        this.view = view
        display = previous.display?.takeIf { it.isValid }?.apply {
            teleport(origin(view))
            text(this@OverlayElement.text)
            rescale(baseScale)
        } ?: return false
        previous.display = null
        return true
    }

    override fun spawn(player: Player, view: PlaneView) {
        this.view = view
        display = player.world.spawn(origin(view), TextDisplay::class.java) { d ->
            d.text(text)
            d.lineWidth = lineWidth
            d.alignment = alignment
            d.isShadowed = false
            d.isSeeThrough = false
            d.isDefaultBackground = false
            d.backgroundColor = background
            d.billboard = Display.Billboard.CENTER
            d.brightness = Display.Brightness(15, 15)
            d.interpolationDuration = 2
            d.isPersistent = false
            d.isVisibleByDefault = false
            d.rescale(baseScale)
        }.also { player.showEntity(abyss, it) }
    }

    override fun hover(on: Boolean) {
        display?.rescale(if (on) baseScale * 1.1f else baseScale)
    }

    override fun remove() {
        display?.remove()
        display = null
    }
}
