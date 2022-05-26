package com.mineinabyss.components.players

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:backpack")
data class Backpack(val backpackContent: MutableList<SerializableItemStack>? = mutableListOf())
