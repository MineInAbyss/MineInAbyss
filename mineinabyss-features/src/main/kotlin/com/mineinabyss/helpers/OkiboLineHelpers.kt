package com.mineinabyss.helpers

import com.combimagnetron.imageloader.Gif
import com.combimagnetron.imageloader.Image

//TODO Should the static images be just in the RP or generated?
// Could either have the low FPS only happen with the gif part or always
// So either consistently bad FPS in Okibo Menu or only on transition portion

fun generateOkiboLineLocationImages() {
    val images = imagesFromUrl() + imagesFromFilePath()
    if (images.isEmpty()) return
}

fun generateOkiboLineTransitionGIFs() {
    val gifs = gifsFromUrl() + gifsFromFilePath()
    if (gifs.isEmpty()) return
}

private fun imagesFromUrl(): List<Image> {
    return emptyList()
}

private fun imagesFromFilePath(): List<Image> {
    return emptyList()
}

private fun gifsFromUrl(): List<Gif> {
    return emptyList()
}

private fun gifsFromFilePath(): List<Gif> {
    return emptyList()
}


