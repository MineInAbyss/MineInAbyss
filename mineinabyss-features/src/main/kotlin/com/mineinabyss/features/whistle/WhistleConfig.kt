package com.mineinabyss.features.whistle

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode
import kotlinx.serialization.Serializable


@Serializable
class WhistleData(
    val name: String,
    val model: String,
    @EncodeDefault(Mode.NEVER)
    val isDefault: Boolean = false,
    // todo: buff config
)

@Serializable
class WhistleConfig(
    val whistles: Map<String, WhistleData> = mapOf(
        "bell" to WhistleData("bell", "mineinabyss:bellModel"),
        "redWhistle" to WhistleData("redWhistle", "mineinabyss:redWhistleModel"),
        "blueWhistle" to WhistleData("blueWhistle", "mineinabyss:blueWhistleModel"),
        "moonWhistle" to WhistleData("moonWhistle", "mineinabyss:moonWhistleModel"),
        "blackWhistle" to WhistleData("blackWhistle", "mineinabyss:blackWhistleModel"),
        "whiteWhistle" to WhistleData("whiteWhistle", "mineinabyss:whiteWhistleModel"),
    )
) {
}


// config ex:

//whistle.yml:
/*
bell:
    model = mineinabyss:bell
    isDefault= true
red_whistle:
    mineinabyss:bellwhistle
 */


