package com.derongan.minecraft.mineinabyss.Relic.Behaviour.Behaviours;

import com.derongan.minecraft.mineinabyss.Relic.Behaviour.ArmorStandBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.DecayableRelicBehaviour;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

/**
 * We treat spawned relics as lootable relics
 */
public class LootableRelicBehaviour implements DecayableRelicBehaviour, ArmorStandBehaviour{
    @Override
    public void onManipulateArmorStand(PlayerArmorStandManipulateEvent e) {
        DecayableRelicBehaviour.registeredRelics.remove(e.getRightClicked().getUniqueId());
        ArmorStandBehaviour.registeredRelics.remove(e.getRightClicked().getUniqueId());

        e.getPlayer().getInventory().addItem(e.getRightClicked().getItemInHand());
        e.getRightClicked().remove();
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
