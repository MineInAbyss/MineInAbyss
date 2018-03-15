package com.derongan.minecraft.mineinabyss.plugin.Relic.Behaviour.Behaviours;

import com.derongan.minecraft.mineinabyss.API.Relic.Behaviour.InteractEntityBehaviour;
import com.derongan.minecraft.mineinabyss.API.Relic.Behaviour.UseRelicBehaviour;
import net.minecraft.server.v1_12_R1.EntityLiving;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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
