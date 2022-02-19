package com.mineinabyss.relics

import com.mineinabyss.components.relics.GhostSeek
import com.mineinabyss.geary.autoscan.AutoScan
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.building.get
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.inventivetalent.glow.GlowAPI
import kotlin.time.Duration.Companion.seconds

@AutoScan
class GhostSeekListener : TickingSystem(interval = 1.seconds) {

    private val TargetScope.player by get<Player>()

    override fun TargetScope.tick() {
        val gearyItem = player.inventory.helmet?.toGearyOrNull(player)
        val ghostSeek = gearyItem?.get<GhostSeek>()
        val entities = ghostSeek?.distance?.let {range -> player.getNearbyEntities(range, range, range) }


        // For coloring the glow according to entities
        //TODO Differentiate depending on MobzyMobType
        //TODO Glowing entities ones loaded will only reglow if item is re-equipped
        //TODO Unhighlight entities outside of range.
        entities?.forEach { entity ->
            // Remove glow if item is removed
            if (!gearyItem.getComponents().contains(GhostSeek)) {
                GlowAPI.setGlowing(entity, false, player)
                return@forEach
            }
            // Return if entity is already applied glowing to
            //if (GlowAPI.isGlowing(entity, player)) return@forEach

            when (entity) {
                is Player -> {
                    GlowAPI.setGlowing(entity, ghostSeek.playerColor, player)
                    GlowAPI.isGlowing(entity, player).broadcastVal("Player: ")
                }
                is LivingEntity -> {
                    GlowAPI.setGlowing(entity, ghostSeek.passiveMobColor, player)
                    GlowAPI.isGlowing(entity, player).broadcastVal("Non-Player: ")
                }
            }
        }
    }

}