package com.mineinabyss.features.guidebook

import kotlinx.serialization.Serializable

@Serializable
data class GuideBookConfig(
    val frontPage: GuideBookPage
)