package com.derongan.minecraft.mineinabyss.Relic.Relics;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.List;

public class BlazeReapRelicType extends AbstractRelicType {
    public BlazeReapRelicType(AbyssContext context) {
        super(Material.DIAMOND_PICKAXE, 1, context);
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Block target = event.getClickedBlock();

        if (target != null && player != null) {
            doBlaze(player, target);
        }
    }

    public void doBlaze(Player player, Block target){
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

    @Override
    public String getName() {
        return "Blaze Reap";
    }

    @Override
    public List<String> getLore() {
        return Arrays.asList("An abnormally large pickaxe that contains Everlasting Gunpowder");
    }
}
