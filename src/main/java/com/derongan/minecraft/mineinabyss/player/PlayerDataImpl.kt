package com.derongan.minecraft.mineinabyss.player

import com.derongan.minecraft.mineinabyss.ascension.effect.AscensionEffect
import com.derongan.minecraft.mineinabyss.whistles.WhistleType
import com.derongan.minecraft.mineinabyss.world.Layer
import org.bukkit.entity.Player
import java.util.*

class PlayerDataImpl(override val player: Player) : PlayerData {
    override var currentLayer: Layer? = null
    override var isAffectedByCurse = true
    override var isAnchored: Boolean = false
    override var isIngame: Boolean = false
    override var curseAccululated = 0.0
    override var exp = 0.0
    override var expOnDescent = 0.0
    override var whistle: WhistleType = WhistleType.BELL
    override var descentDate: Date? = null
    private val effects: MutableList<AscensionEffect> = mutableListOf()
    override val ascensionEffects: List<AscensionEffect>
        get() = effects.toList()
    override val level: Int
        get() = exp.toInt() / 10 //TODO write a proper formula

    override fun addAscensionEffect(effect: AscensionEffect) {
        effects.add(effect)
    }

    override fun addExp(exp: Double) {
        this.exp += exp
    }
}