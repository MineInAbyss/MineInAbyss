package com.derongan.minecraft.mineinabyss.plugin.Relic.Behaviour.Behaviours;

import com.derongan.minecraft.mineinabyss.API.Relic.Behaviour.ArmorStandBehaviour;
import com.derongan.minecraft.mineinabyss.API.Relic.Behaviour.DecayableRelicBehaviour;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

/**
 * We treat spawned relics as lootable relics
 */
public class LootableRelicBehaviour implements DecayableRelicBehaviour, ArmorStandBehaviour{
    @Override
    public void onPlayerInteractEntity(PlayerInteractAtEntityEvent e) {
        DecayableRelicBehaviour.registeredRelics.remove(e.getRightClicked().getUniqueId());
        ArmorStandBehaviour.registeredRelics.remove(e.getRightClicked().getUniqueId());
        ArmorStand as = (ArmorStand) e.getRightClicked();
        e.getPlayer().getInventory().addItem(as.getItemInHand());
        as.remove();
        e.setCancelled(true);
    }

    @Override
    public void onDecay(RelicInfo toDecay, int ticks) {
        toDecay.lived += ticks;

        ArmorStand armorStand = (ArmorStand)toDecay.entity;

        if(toDecay.lived >= toDecay.lifeTime){
            ArmorStandBehaviour.registeredRelics.remove(armorStand.getUniqueId());
            DecayableRelicBehaviour.registeredRelics.remove(armorStand.getUniqueId());
            armorStand.remove();
        }
    }
}
