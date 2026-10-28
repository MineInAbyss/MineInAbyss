package com.mineinabyss.components.mobs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** @property style a style from the mob health config, falling back to its default style when not defined there */
@JvmInline
@Serializable
@SerialName("mineinabyss:show_health_bar")
value class ShowHealthBar(val style: String)
