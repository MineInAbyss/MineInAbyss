package com.mineinabyss.relics

import com.mineinabyss.components.guilds.WhistleRank
import com.mineinabyss.components.relics.UnheardBell
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class UnheardBellListener : Listener {

    //@EventHandler

    @EventHandler
    fun PlayerInteractAtEntityEvent.spawnUnheardBell() {
        val gearyEntity = rightClicked.toGearyOrNull() ?: return
        val unheardBell = gearyEntity.get<UnheardBell>() ?: return

        if (player.toGeary().get<WhistleRank>()?.rank == unheardBell.whistleRequirement) {
            Bukkit.getOnlinePlayers().forEach {
                it.playSound(it.location, "mineinabyss:relic.unheard_bell_rung", 1f, 1f)
            }
            player.location.getNearbyLivingEntities(unheardBell.effectRange).forEach { entity ->
                entity.addPotionEffect(PotionEffect(PotionEffectType.SLOW, unheardBell.effectDuration, Int.MAX_VALUE, false, false))
                entity.addPotionEffect(PotionEffect(PotionEffectType.SLOW_DIGGING, unheardBell.effectDuration, Int.MAX_VALUE, false, false))
                entity.addPotionEffect(PotionEffect(PotionEffectType.JUMP, unheardBell.effectDuration, Int.MAX_VALUE, false, false))
            }
        }
        else {
            player.playSound(player.location, "mineinabyss:relic.unheard_bell_denied", 1f, 1f)
            player.sendMessage(Component.text("The Abyss laughs at your naivety").color(TextColor.color(255, 163, 26)))
        }


    }
}