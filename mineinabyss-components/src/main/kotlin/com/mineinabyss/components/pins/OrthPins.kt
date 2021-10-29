package com.mineinabyss.components.pins

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import com.mineinabyss.geary.ecs.prefab.PrefabKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:orth_pins")
@AutoscanComponent
data class OrthPins(
    val selected: MutableSet<PrefabKey> = mutableSetOf(),
    var maximum: Int = 1,
) {
    fun selectPin(key: PrefabKey) {
        if(key in selected) return
        if(selected.size >= maximum)
            selected -= selected.last()
        selected += key
    }
}
