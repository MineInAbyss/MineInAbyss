package com.derongan.minecraft.mineinabyss.ecs.components.pins

import com.derongan.minecraft.mineinabyss.services.AbyssWorldManager
import com.derongan.minecraft.mineinabyss.world.LayerKey
import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("mineinabyss:pin_drop")
@AutoscanComponent
class PinDrop(
    val layerKey: LayerKey
) {
    @Transient
    val layer = AbyssWorldManager.getLayerFor(layerKey)
}
