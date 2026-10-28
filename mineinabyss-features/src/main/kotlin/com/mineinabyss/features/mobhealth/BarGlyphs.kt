package com.mineinabyss.features.mobhealth

import com.mineinabyss.emojy.config.Emote
import com.mineinabyss.emojy.spaceComponent
import com.mineinabyss.features.overlay.Emotes
import net.kyori.adventure.text.Component

class BarGlyphs(style: HealthBarStyle) {
    private val fillEmote = Emotes.get(style.fillEmote)
    private val trailEmote = Emotes.get(style.trailEmote)
    private val inset = spaceComponent(style.fillInset)
    private val padding = spaceComponent(style.fillPadding)

    val frame = Emotes.component(Emotes.get(style.frameEmote))
    val steps = fillEmote.unicodes.size - 1

    // Every sheet row is padded to the full bar width, so a fill glyph advances one pixel past its last step
    val linePixels = style.fillInset + steps + 1 + style.fillPadding

    fun fill(step: Int) = row(fillEmote, step)

    fun trail(step: Int) = row(trailEmote, step)

    private fun row(emote: Emote, step: Int) =
        Component.textOfChildren(inset, Component.text(emote.unicodes[step]).font(emote.font), padding)
}
