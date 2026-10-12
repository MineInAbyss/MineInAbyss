package com.mineinabyss.features.helpers

import com.mineinabyss.dependencies.DI
import com.mineinabyss.dependencies.getOrNull
import com.mineinabyss.dependencies.scope
import com.mineinabyss.features.abyss

inline fun <reified T> loaded(feature: DI.Module): T? = abyss.scope.getOrNull(feature)?.getOrNull<T>()
