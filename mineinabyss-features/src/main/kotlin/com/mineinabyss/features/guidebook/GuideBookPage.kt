package com.mineinabyss.features.guidebook

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.Serializable

@Serializable
data class GuideBookPage(
    val title: String,
    val buttons: List<GuideBookButton>
)