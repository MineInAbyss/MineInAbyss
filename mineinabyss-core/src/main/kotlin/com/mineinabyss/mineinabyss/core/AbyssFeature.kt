package com.mineinabyss.mineinabyss.core

interface AbyssFeature {
    val dependsOn: Set<String> get() = setOf()

    fun MineInAbyssPlugin.enableFeature() {}

    fun MineInAbyssPlugin.disableFeature() {}
}
