package com.mineinabyss.components.pins

import com.mineinabyss.components.LayerKey
import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:pin_drop")
@AutoscanComponent
class PinDrop(
    val layerKey: LayerKey
)
