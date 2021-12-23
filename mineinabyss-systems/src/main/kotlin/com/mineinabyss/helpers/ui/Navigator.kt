package com.mineinabyss.helpers.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

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

@Composable
fun <T> rememberNavigation(default: () -> T) = remember {
    Navigator(default)
}
