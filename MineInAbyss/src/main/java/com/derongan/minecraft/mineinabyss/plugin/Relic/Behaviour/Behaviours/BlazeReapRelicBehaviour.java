package com.derongan.minecraft.mineinabyss.plugin.Relic.Behaviour.Behaviours;

import com.derongan.minecraft.mineinabyss.API.Relic.Behaviour.EntityHitRelicBehaviour;
import com.derongan.minecraft.mineinabyss.API.Relic.Behaviour.UseRelicBehaviour;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlazeReapRelicBehaviour implements EntityHitRelicBehaviour, UseRelicBehaviour {
    @Override
    public void onHit(EntityDamageByEntityEvent event) {
        doBlaze((Player)event.getDamager(), event.getEntity().getLocation().getBlock());
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Block target = event.getClickedBlock();

        if (target != null && player != null) {
            doBlaze(player, target);

            event.setCancelled(true);
        }
    }


    private void doBlaze(Player player, Block target) {
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getRelative(BlockFace.UP).getLocation(), 3);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getRelative(BlockFace.DOWN).getLocation(), 3);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getRelative(BlockFace.EAST).getLocation(), 3);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getRelative(BlockFace.WEST).getLocation(), 3);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getRelative(BlockFace.SOUTH).getLocation(), 3);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getRelative(BlockFace.NORTH).getLocation(), 3);
        player.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, target.getLocation(), 3);

        player.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2f, .7f);

        player.getWorld().getNearbyEntities(target.getLocation(), 2, 2, 2).forEach(b -> {
            if (b instanceof LivingEntity)
                ((LivingEntity) b).damage(10);
        });
    }
}
