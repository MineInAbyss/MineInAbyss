package com.mineinabyss.components.lootcrates

import com.mineinabyss.idofront.serialization.KeySerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

@Serializable
@SerialName("mineinabyss:crate_sounds")
class CrateSounds(
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val open: @Serializable(KeySerializer::class) Key = Key.key("block.chest.open"),
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val close: @Serializable(KeySerializer::class) Key = Key.key("block.chest.close"),
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val empty: @Serializable(KeySerializer::class) Key = Key.key("block.decorated_pot.insert_fail"),
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val volume: Float = 0.5f,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val pitch: Float = 1f,
) {
    @Transient
    val openSound = Sound.sound(open, Sound.Source.BLOCK, volume, pitch)

    @Transient
    val closeSound = Sound.sound(close, Sound.Source.BLOCK, volume, pitch)

    @Transient
    val emptySound = Sound.sound(empty, Sound.Source.BLOCK, volume, pitch)

    companion object {
        val DEFAULT = CrateSounds()
    }
}
