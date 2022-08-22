package com.mineinabyss.relics.depthmeter

import com.mineinabyss.components.helpers.HideDepthMeterHud
import com.mineinabyss.components.relics.DepthMeter
import com.mineinabyss.components.relics.StarCompass
import com.mineinabyss.geary.annotations.AutoScan
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.components.events.EntityRemoved
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.helpers.toggleHud
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.time.ticks
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.ItemStack

@AutoScan
class DepthHudSystem : RepeatingSystem(1.ticks), Listener {
    private val TargetScope.starCompass by get<StarCompass>()
    private val TargetScope.item by get<ItemStack>()

    override fun TargetScope.tick() {
        val player = entity.parent?.get<Player>() ?: return
        broadcast("${player.name} ticked")
        player.toggleHud("depth", !player.toGeary().has<HideDepthMeterHud>())
    }

    @EventHandler
    fun PlayerDropItemEvent.onDropDepthMeter() {
        if (itemDrop.toGearyOrNull()?.has<DepthMeter>().broadcastVal() != true) return
        broadcast("${player.name} dropped a depth meter")
        //player.toggleHud("depthmeter", false)
    }

    @EventHandler
    fun PlayerAttemptPickupItemEvent.onPickUpDepthMeter() {
        if (!item.toGeary().has<DepthMeter>()) return
        broadcast("${player.name} picked up a depth meter")
        //player.toggleHud("depthmeter", !player.toGeary().has<HideDepthMeterHud>())
    }
}

@AutoScan
class RemoveDepthMeterHud : GearyListener() {
    private val TargetScope.depthMeter by get<DepthMeter>()
    private val EventScope.removed by family { has<EntityRemoved>() }

    @Handler
    fun TargetScope.removeBar() {
        val player = entity.parent?.get<Player>() ?: return
        player.toggleHud("depth", player.toGeary().has<HideDepthMeterHud>())
    }
}
