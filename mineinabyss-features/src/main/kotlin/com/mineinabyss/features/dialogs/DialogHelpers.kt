package com.mineinabyss.features.dialogs

import net.kyori.adventure.key.Key

fun String.joinToString(separator: CharSequence = ", "): String {
    return toList().joinToString(separator)
}
object DialogHelpers {
    val dialogId = Key.key("mia_dialog")
    val okiboMarker = Key.key("okibo_marker")
    val okiboMarkerConfirm = Key.key("okibo_marker_confirm")
}