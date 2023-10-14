package com.mineinabyss.mineinabyss.core

import com.mineinabyss.idofront.config.IdofrontConfig

interface Configurable<T> {
    val configManager: IdofrontConfig<T>
    val config: T get() = configManager.getOrLoad()
}
