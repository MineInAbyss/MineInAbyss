package com.mineinabyss.mineinabyss.core

interface AbyssFeature {
    //TODO
    val dependsOn: Set<String> get() = setOf()

    fun MineInAbyssPlugin.enableFeature() {}

    fun MineInAbyssPlugin.disableFeature() {}
}
