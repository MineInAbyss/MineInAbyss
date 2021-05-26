package com.derongan.minecraft.mineinabyss.ecs.components

import com.derongan.minecraft.mineinabyss.services.AbyssWorldManager
import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:pin_drop")
@AutoscanComponent
class PinDrop(
    val layerName: String
) {
    val layer = AbyssWorldManager.getLayerFor(layerName)
}
