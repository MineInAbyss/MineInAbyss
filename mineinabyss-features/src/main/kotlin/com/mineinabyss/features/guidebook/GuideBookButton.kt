package com.mineinabyss.features.guidebook

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.Serializable

@Serializable
data class GuideBookButton(
    val buttonItem: SerializableItemStack,
    val buttonAction: Action,
    val value: String
) {
    enum class Action {
        NEW_PAGE, CHANGE_TITLE
    }
}