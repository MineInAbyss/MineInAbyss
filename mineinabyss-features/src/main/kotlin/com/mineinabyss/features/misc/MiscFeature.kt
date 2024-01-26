package com.mineinabyss.features.misc

import com.mineinabyss.components.playerData
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners

class MiscFeature : Feature() {
    override fun FeatureDSL.enable() {
        plugin.listeners(MiscListener())
    }
}
