package com.mineinabyss.features.mobhealth

import com.mineinabyss.emojy.config.Emote
import com.mineinabyss.emojy.spaceComponent
import com.mineinabyss.features.overlay.Emotes
import net.kyori.adventure.text.Component

class BarGlyphs(config: MobHealthConfig) {
    private val fillEmote = Emotes.get(config.fillEmote)
    private val trailEmote = Emotes.get(config.trailEmote)
    private val inset = spaceComponent(config.fillInset)
    private val padding = spaceComponent(config.fillPadding)

    val frame = Emotes.component(Emotes.get(config.frameEmote))
    val steps = fillEmote.unicodes.size - 1

    // Every sheet row is padded to the full bar width, so a fill glyph advances one pixel past its last step
    val linePixels = config.fillInset + steps + 1 + config.fillPadding

    fun fill(step: Int) = row(fillEmote, step)

    fun trail(step: Int) = row(trailEmote, step)

    private fun row(emote: Emote, step: Int) =
        Component.textOfChildren(inset, Component.text(emote.unicodes[step]).font(emote.font), padding)
}
