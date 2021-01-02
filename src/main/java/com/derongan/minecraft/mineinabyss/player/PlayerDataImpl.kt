package com.derongan.minecraft.mineinabyss.player

import com.derongan.minecraft.mineinabyss.ascension.effect.AscensionEffect
import com.derongan.minecraft.mineinabyss.world.Layer
import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

@Serializable
class PlayerDataImpl(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID, //TODO pass this from file name somehow
    @SerialName("affectable")
    override var isAffectedByCurse: Boolean = true,
    @SerialName("ingame")
    override var isIngame: Boolean = false,
    @SerialName("ascended")
    override var curseAccrued: Double = 0.0,
    override var exp: Double = 0.0,
    override var expOnDescent: Double = 0.0,
    @Serializable(with = DateAsLongSerializer::class)
    override var descentDate: Date? = null,
) : PlayerData {
    @Transient
    override var currentLayer: Layer? = null

    @Transient
    override val player: Player = Bukkit.getPlayer(uuid) ?: error("UUID of player data doesn't match a player")

    @Transient
    private val effects: MutableList<AscensionEffect> = mutableListOf()
    override val ascensionEffects get() = effects.toList()
    override val level: Int get() = exp.toInt() / 10 //TODO write a proper formula

    override fun addAscensionEffect(effect: AscensionEffect) {
        effects.add(effect)
    }

    override fun addExp(exp: Double) {
        this.exp += exp
    }
}

object DateAsLongSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: Date) = encoder.encodeLong(value.time)
    override fun deserialize(decoder: Decoder): Date = Date(decoder.decodeLong())
}
