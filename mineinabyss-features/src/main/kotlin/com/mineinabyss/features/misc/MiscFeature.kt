package com.mineinabyss.features.misc

import com.mineinabyss.dependencies.module
import com.mineinabyss.idofront.features.listeners

val MiscFeature = module("miscellaneous") {
    listeners(MiscListener())
}
