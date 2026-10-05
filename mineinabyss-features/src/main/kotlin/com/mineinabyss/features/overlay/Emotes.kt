package com.mineinabyss.features.overlay

import com.mineinabyss.emojy.config.Emote
import com.mineinabyss.emojy.emojy
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.math.roundToInt

object Emotes {
    // The opaque part of a glyph in text pixels from its top left
    data class Bounds(val left: Double, val top: Double, val right: Double, val bottom: Double)

    private val bounds = mutableMapOf<Emote, Bounds>()

    fun find(id: String): Emote? = emojy.emotes.firstOrNull { it.id == id }

    fun get(id: String): Emote = find(id) ?: error("Emojy has no emote named $id")

    fun component(emote: Emote): Component = emote.formattedComponent(insert = false)

    // The client trims transparent columns off the right of a bitmap glyph
    fun width(emote: Emote): Int = bounds(emote).right.roundToInt()

    fun bounds(emote: Emote): Bounds = bounds.getOrPut(emote) {
        val full = Bounds(0.0, 0.0, emote.height.toDouble(), emote.height.toDouble())
        val image = texture(emote) ?: return@getOrPut full
        val opaque = (0 until image.width).flatMap { x -> (0 until image.height).map { y -> x to y } }
            .filter { (x, y) -> image.getRGB(x, y) ushr 24 != 0 }
            .ifEmpty { return@getOrPut full }
        val px = emote.height / image.height.toDouble()
        Bounds(opaque.minOf { it.first } * px, opaque.minOf { it.second } * px, (opaque.maxOf { it.first } + 1) * px, (opaque.maxOf { it.second } + 1) * px)
    }

    // Emojy finds a texture by its file name anywhere in its emotes folder
    private fun texture(emote: Emote): BufferedImage? {
        val name = emote.texture.value().substringAfterLast('/')
        return Bukkit.getPluginManager().getPlugin("Emojy")?.dataFolder?.resolve("emotes")
            ?.walkTopDown()?.firstOrNull { it.isFile && it.name == name }
            ?.let(ImageIO::read)
    }
}
