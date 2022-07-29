package com.mineinabyss.components.fasttravel.okiboline

import com.mineinabyss.geary.papermc.access.toGeary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("mineinabyss:okiboline")
class OkiboLine(
    var useGifForMenuTransition: Boolean = true,
    var currentPoint: Int = 1,
)

val Player.okiboLine get() = toGeary().getOrSetPersisting { OkiboLine() }
