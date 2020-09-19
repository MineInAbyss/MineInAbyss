package com.derongan.minecraft.mineinabyss.player

import com.derongan.minecraft.mineinabyss.ascension.effect.AscensionEffect
import com.derongan.minecraft.mineinabyss.world.Layer
import org.bukkit.entity.Player
import java.util.*

/**
 * @property player The Bukkit player for this data.
 * @property currentLayer The layer the player is on.
 *
 * Change with caution. You are responsible for moving the player so data is not out of sync.
 * @property isAffectedByCurse Whether the player is affected by the curse of ascending in the abyss.
 * @property ascensionEffects The mutable list of current effects on this player.
 * @property curseAccrued The distance the player has ascended since last being affected by the curse.
 *
 * TODO should this be exposed like this?
 * @property whistle The type of whistle the player currently holds.
 * @property level The player's current level. Increases as the player earns exp. For now, it is only cosmetic.
 * @property exp The amount of experience the player currently has. This is not related to Minecraft's existing
 * experience system. It increases as the player does actions in the abyss.
 * @property expOnDescent The amount of xp the player started with when beginning their descent.
 * @property isIngame Whether the player is currently descending.
 */
interface PlayerData {
    val player: Player
    var currentLayer: Layer?
    var isAffectedByCurse: Boolean
    val ascensionEffects: List<AscensionEffect>
    var curseAccrued: Double
    val level: Int
    var exp: Double
    var expOnDescent: Double
    var descentDate: Date?
    var isIngame: Boolean

    /**
     * Add an effect to the player
     *
     * @param effect the effect to add
     */
    fun addAscensionEffect(effect: AscensionEffect)

    /**
     * Add exp to the player
     *
     * @param exp the amount of exp to add
     */
    fun addExp(exp: Double)
}