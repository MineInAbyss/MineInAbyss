package com.derongan.minecraft.mineinabyss.relic.behaviour.behaviours;

import com.derongan.minecraft.mineinabyss.relic.behaviour.CooldownRelicBehaviour;
import com.derongan.minecraft.mineinabyss.relic.behaviour.EntityHitRelicBehaviour;
import com.derongan.minecraft.mineinabyss.relic.behaviour.UseRelicBehaviour;
import com.derongan.minecraft.mineinabyss.relic.relics.RelicType;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlazeReapRelicBehaviour implements EntityHitRelicBehaviour, UseRelicBehaviour, CooldownRelicBehaviour {
    RelicType type;
    boolean onCooldown;

    @Override
    public void onHit(EntityDamageByEntityEvent event) {
        if (onCooldown) {
            return;
        }
        doBlaze((Player)event.getDamager(), event.getEntity().getLocation().getBlock());
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block target = event.getClickedBlock();

        if (target != null && player != null) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
            if (!onCooldown) {
                doBlaze(player, target);
                CooldownRelicBehaviour.registerCooldown(event.getPlayer(), 100, type);
                onCooldown = true;
            }
        }
    }

    @Override
    public void cooledDown() {
        onCooldown = false;
    }

    private void doBlaze(Player player, Block target) {
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getRelative(BlockFace.UP).getLocation(), 3);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getRelative(BlockFace.DOWN).getLocation(), 3);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getRelative(BlockFace.EAST).getLocation(), 3);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getRelative(BlockFace.WEST).getLocation(), 3);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getRelative(BlockFace.SOUTH).getLocation(), 3);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getRelative(BlockFace.NORTH).getLocation(), 3);
        player.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, target.getLocation(), 3);
        player.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.7f);
        player.getWorld().getNearbyEntities(target.getLocation(), 2.0, 2.0, 2.0).forEach(b -> {
            if (b instanceof LivingEntity) {
                ((LivingEntity)b).damage(10.0);
            }
        });
    }

    @Override
    public void setRelicType(RelicType type) {
        this.type = type;
    }
}