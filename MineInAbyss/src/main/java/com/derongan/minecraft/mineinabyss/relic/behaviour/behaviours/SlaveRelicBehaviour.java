package com.derongan.minecraft.mineinabyss.relic.behaviour.behaviours;

import com.derongan.minecraft.mineinabyss.relic.behaviour.InteractEntityBehaviour;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class SlaveRelicBehaviour implements InteractEntityBehaviour{
    @Override
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity other = event.getRightClicked();

        if(other instanceof LivingEntity){
            other.addPassenger(player);
            ((LivingEntity) other).setAI(false);


        }
    }
}
