package com.derongan.minecraft.mineinabyss.Relic;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RelicUseListener implements Listener {
    AbyssContext context;

    Set<Material> passable = Stream.of(Material.AIR,
            Material.LEAVES,
            Material.LEAVES_2,
            Material.YELLOW_FLOWER,
            Material.VINE,
            Material.DIRT,
            Material.GRASS,
            Material.RED_ROSE,
            Material.SAPLING,
            Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM,
            Material.DOUBLE_PLANT).collect(Collectors.toSet());

    public RelicUseListener(AbyssContext context) {
        this.context = context;
    }

    boolean hooked = false;
    Vector hookLocation = null;

    @EventHandler()
    public void onPlayerFish(PlayerFishEvent playerFishEvent) {
        Vector fishLoc = playerFishEvent.getHook().getLocation().toVector();

        Block block = playerFishEvent.getPlayer().getWorld().getBlockAt(fishLoc.toLocation(playerFishEvent.getPlayer().getWorld()));
        if (playerFishEvent.getState().equals(PlayerFishEvent.State.IN_GROUND)) {
            playerFishEvent.setCancelled(true);
            hookLocation = fishLoc.add(new Vector(0, 2, 0));
            hooked = true;
        } else {
            hooked = false;
        }
    }

    @EventHandler()
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
        Player player = playerMoveEvent.getPlayer();

        if (playerMoveEvent.getTo().getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.AIR) && hooked) {
            if (playerMoveEvent.getTo().toVector().subtract(playerMoveEvent.getFrom().toVector()).getY() > 0) {
                if (playerMoveEvent.getTo().toVector().subtract(hookLocation).length() <= 2) {
                    player.setFlying(false);
                    return;
                }

                player.setFlying(true);
                if (playerMoveEvent.getTo().toVector().getY() > hookLocation.getY()) {
                    playerMoveEvent.getTo().setY(hookLocation.getY());
                }
                playerMoveEvent.getPlayer().setVelocity(hookLocation.clone().subtract(playerMoveEvent.getFrom().toVector()).normalize().multiply(.4));
            } else {
                player.setFlying(false);
            }
        }
    }

    @EventHandler()
    public void onPlayerUseItem(PlayerInteractEvent playerInteractEvent) {
        if ((playerInteractEvent.getAction() == Action.RIGHT_CLICK_AIR || playerInteractEvent.getAction() == Action.RIGHT_CLICK_BLOCK) && playerInteractEvent.hasItem()) {
            if (hooked) {
//                playerInteractEvent.getPlayer().setVelocity(hookLocation.subtract(playerInteractEvent.getPlayer().getLocation().toVector()).normalize().multiply(.5));
            }
            ItemStack theItemStack = playerInteractEvent.getItem();

            if (theItemStack.getType().equals(Material.STICK)) {
//                context.getPlugin().getServer().broadcastMessage("Spell cast");

                Player player = playerInteractEvent.getPlayer();

                List<Block> blocks = player.getLineOfSight(passable, 100);


                blocks.subList(10, blocks.size()).forEach(a -> {
                    for (int i = -2; i < 5; i++) {
                        for (int j = -2; j < 5; j++) {
                            for (int k = -2; k < 5; k++) {

                                Block relative = a.getRelative(i, j, k);

                                if (Math.random() > .999)
                                    player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, relative.getLocation(), 1);

                                if (Math.abs(i) == 2 || Math.abs(j) == 2 || Math.abs(k) == 2) {
                                    if (relative.getType() != Material.AIR) {
                                        relative.setType(Material.COAL_BLOCK);
                                    }
                                } else {
                                    if (Math.random() > .1)
                                        relative.setType(Material.AIR);
                                    else
                                        relative.setType(Material.FIRE);

                                }
                                if (i == 0 && j == 0 && k == 0) {
                                    if (Math.random() > .5) {
                                        player.getWorld().spawnParticle(Particle.SMOKE_LARGE, relative.getLocation(), 1);
                                        player.getWorld().playSound(relative.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 5f, .5f);
                                        player.getWorld().playSound(relative.getLocation(), Sound.ENTITY_LIGHTNING_IMPACT, 5f, .5f);
                                    }

                                    player.getWorld().getNearbyEntities(relative.getLocation(), 3, 3, 3).forEach(b -> {
                                        if (b instanceof LivingEntity)
                                            ((LivingEntity) b).damage(100);
                                    });
                                }
                            }
                        }
                    }
                });
            } else if (theItemStack.getType().equals(Material.FISHING_ROD)) {

            }
        } else {

        }
    }
}
