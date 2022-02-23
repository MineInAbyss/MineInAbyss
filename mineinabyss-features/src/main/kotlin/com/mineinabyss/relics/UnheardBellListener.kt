package com.mineinabyss.relics

import com.mineinabyss.components.guilds.WhistleRank
import com.mineinabyss.components.relics.UnheardBell
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.geary.papermc.spawnFromPrefab
import com.mineinabyss.geary.papermc.toPrefabKey
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.idofront.util.toMCKey
import com.mineinabyss.looty.tracking.toGearyOrNull
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerLeashEntityEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class UnheardBellListener : Listener {

    @EventHandler
    fun PlayerInteractEvent.placingUnheardBell() {
        if (hand != EquipmentSlot.HAND) return
        if (!rightClicked) return
        if (clickedBlock == null) return
        if (blockFace == BlockFace.DOWN) return
        val item = player.inventory.itemInMainHand
        val gearyItem = item.toGearyOrNull(player) ?: return
        val unheardBell = gearyItem.get<UnheardBell>() ?: return
        val loc = player.getLastTwoTargetBlocks(null, 6)

        val newLoc =
            if (blockFace == BlockFace.UP)
                loc.last()?.location?.toCenterLocation()?.apply { y += 0.5 } ?: return
            else
                loc.first()?.location?.toCenterLocation()?.apply { y -= 0.5 } ?: return

        val entityPrefab = "mineinabyss:unheardbell".toMCKey().toPrefabKey()

        item.subtract(1)
        newLoc.spawnFromPrefab(entityPrefab)
        player.playSound(newLoc, unheardBell.soundDenied, 1f, 1f)
    }

    @EventHandler
    fun PlayerInteractAtEntityEvent.spawnUnheardBell() {
        val gearyEntity = rightClicked.toGearyOrNull() ?: return
        val unheardBell = gearyEntity.get<UnheardBell>() ?: return

        if (player.toGeary().get<WhistleRank>()?.rank == unheardBell.whistleRequirement) {
            Bukkit.getOnlinePlayers().forEach {
                it.playSound(it.location, unheardBell.soundRung, 1f, 1f)
            }
            player.location.getNearbyLivingEntities(unheardBell.effectRange).forEach { entity ->
                entity.addPotionEffect(PotionEffect(PotionEffectType.SLOW, unheardBell.effectDuration, Int.MAX_VALUE, false, false))
                entity.addPotionEffect(PotionEffect(PotionEffectType.SLOW_DIGGING, unheardBell.effectDuration, Int.MAX_VALUE, false, false))
                entity.addPotionEffect(PotionEffect(PotionEffectType.JUMP, unheardBell.effectDuration, Int.MAX_VALUE, false, false))
            }
        }
        else {
            player.playSound(player.location, unheardBell.soundDenied, 1f, 1f)
            player.sendMessage(Component.text("The Abyss laughs at your naivety").color(TextColor.color(255, 163, 26)))
        }
    }

    //TODO Wait for Mobzy-API
    @EventHandler
    fun PlayerLeashEntityEvent.onLeashingBell() {
        val gearyEntity = entity.toGearyOrNull() ?: return
        //val modelEntity = entity.toModelEntity() ?: return
        val unheardBell = gearyEntity.get<UnheardBell>() ?: return
    }
}