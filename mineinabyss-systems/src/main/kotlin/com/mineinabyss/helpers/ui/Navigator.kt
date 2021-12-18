package com.mineinabyss.helpers.ui

import androidx.compose.runtime.mutableStateListOf

class Navigator<T>(val default: () -> T) {
    val screen: T? get() = screens.lastOrNull()
    val screens = mutableStateListOf<T>()

    init {
        open(default())
    }

    fun back() = screens.removeLastOrNull()
    fun open(screen: T) = screens.add(screen)
    fun reset() {
        screens.clear()
        open(default())
    }
}
