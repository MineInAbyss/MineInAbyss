package com.derongan.minecraft.mineinabyss.relic.behaviour.behaviours;

import com.derongan.minecraft.mineinabyss.MineInAbyss;
import com.derongan.minecraft.mineinabyss.relic.behaviour.ArmorStandBehaviour;
import com.derongan.minecraft.mineinabyss.relic.behaviour.DecayableRelicBehaviour;
import com.derongan.minecraft.mineinabyss.world.EntityChunkManager;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
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

        removeFromWorld(e.getRightClicked());

        e.setCancelled(true);
    }

    private void removeFromWorld(Entity e){
        EntityChunkManager manager = MineInAbyss.getInstance().getContext().getEntityChunkManager();
        manager.removeEntity(e.getLocation().getChunk(), e);
        e.remove();

        System.out.println("Removing relic");
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
