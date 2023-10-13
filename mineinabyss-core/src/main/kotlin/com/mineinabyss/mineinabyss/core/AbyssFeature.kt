package com.mineinabyss.mineinabyss.core

import com.mineinabyss.idofront.config.IdofrontConfig

interface AbyssFeature {
    val dependsOn: Set<String> get() = setOf()

    fun MineInAbyssPlugin.enableFeature() {}

    fun MineInAbyssPlugin.disableFeature() {}
}

interface Configurable<T> {
    val configManager: IdofrontConfig<T>
    val config: T
}
