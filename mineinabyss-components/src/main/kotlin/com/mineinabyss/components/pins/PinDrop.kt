package com.mineinabyss.components.pins

import com.mineinabyss.components.layer.LayerKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:pin_drop")
class PinDrop(
    val layerKey: LayerKey
)
