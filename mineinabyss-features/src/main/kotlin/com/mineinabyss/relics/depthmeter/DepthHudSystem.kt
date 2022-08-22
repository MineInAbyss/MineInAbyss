package com.mineinabyss.relics.depthmeter

import com.mineinabyss.components.helpers.HideDepthMeterHud
import com.mineinabyss.components.relics.DepthMeter
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.components.events.EntityRemoved
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.SourceScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.helpers.toggleHud
import com.mineinabyss.idofront.time.ticks
import com.mineinabyss.relics.RelicsFeature
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("mineinabyss:toggle_depth_hud")
class ToggleDepthHud

class ToggleDepthHudSystem(val feature: RelicsFeature) : GearyListener() {
    private val TargetScope.player by get<Player>()
    private val SourceScope.depthMeter by get<DepthMeter>()
    private val EventScope.hasDepth by family { has<ToggleDepthHud>() }

    @Handler
    fun TargetScope.toggleDepth(source: SourceScope) {
        if (player.toGeary().has<HideDepthMeterHud>())
            player.toGeary().remove<HideDepthMeterHud>()
        else player.toGeary().setPersisting(HideDepthMeterHud())
    }
}

class DepthHudSystem(private val feature: RelicsFeature) : RepeatingSystem(1.ticks) {
    private val TargetScope.depthMeter by get<DepthMeter>()

    override fun TargetScope.tick() {
        val player = entity.parent?.get<Player>() ?: return
        player.toggleHud(feature.depthHudId, !player.toGeary().has<HideDepthMeterHud>())
    }
}

class RemoveDepthMeterHud(private val feature: RelicsFeature) : GearyListener() {
    private val TargetScope.depthMeter by get<DepthMeter>()
    private val EventScope.removed by family { has<EntityRemoved>() }

    @Handler
    fun TargetScope.removeBar() {
        val player = entity.parent?.get<Player>() ?: return
        player.toggleHud(feature.depthHudId, false)
    }
}
