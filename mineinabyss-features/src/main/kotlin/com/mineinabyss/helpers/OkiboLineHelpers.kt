package com.mineinabyss.helpers

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.mineinabyss.core.mineInAbyss
import com.mineinabyss.okiboline.OkiboLineFeature
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

//TODO Should the static images be just in the RP or generated?
// Could either have the low FPS only happen with the gif part or always
// So either consistently bad FPS in Okibo Menu or only on transition portion

var okiboMenus= listOf<Component>()

fun generateOkiboLineLocationImages(feature: OkiboLineFeature) {
    mineInAbyss.launch { generateOkiboImages(feature) }
}

private fun generateOkiboImages(feature: OkiboLineFeature) {
    val images = mutableListOf<String>()
    for (fileName in feature.okiboImageFileNames) {
        images.add(convertFileToImageString(fileName))
    }

    val menuList = mutableListOf<Component>()
    images.forEach {
        menuList.add(convertToImageComponent(it, Key.key("minecraft", "okibo_fasttravel")))
    }
    okiboMenus = menuList
}
