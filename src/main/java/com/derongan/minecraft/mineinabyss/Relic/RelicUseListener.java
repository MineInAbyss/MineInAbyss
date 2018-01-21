package com.derongan.minecraft.mineinabyss.Relic;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Relic.Relics.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RelicUseListener implements Listener {
    AbyssContext context;

    UnheardBellRelicType unheardBellRelicType;

    public static Set<Material> passable = Stream.of(Material.AIR,
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
        unheardBellRelicType = new UnheardBellRelicType(context);
    }

    boolean hooked = false;
    Vector hookLocation = null;

    @EventHandler()
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
        playerJoinEvent.getPlayer().setCompassTarget(new Location(playerJoinEvent.getPlayer().getWorld(), 0, -10000, 0));
    }

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
//
//        if (playerMoveEvent.getTo().getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.AIR) && hooked) {
//            if (playerMoveEvent.getTo().toVector().subtract(playerMoveEvent.getFrom().toVector()).getY() > 0) {
//                if (playerMoveEvent.getTo().toVector().subtract(hookLocation).length() <= 2) {
//                    player.setFlying(false);
//                    return;
//                }
//
//                player.setFlying(true);
//                if (playerMoveEvent.getTo().toVector().getY() > hookLocation.getY()) {
//                    playerMoveEvent.getTo().setY(hookLocation.getY());
//                }
//                playerMoveEvent.getPlayer().setVelocity(hookLocation.clone().subtract(playerMoveEvent.getFrom().toVector()).normalize().multiply(.4));
//            } else {
//                player.setFlying(false);
//            }
//        }
    }

    @EventHandler()
    public void onEntityHit(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damagee = event.getEntity();

        if (damager instanceof Player) {
            if (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                if (new PushStickRelicType(context).isRelicItem(((Player) damager).getInventory().getItemInMainHand())) {
                    Vector damagerVec = damager.getLocation().toVector();
                    Vector damageeVec = damagee.getLocation().toVector();

                    // Vector pointing towards damagee
                    Vector pushDir = damageeVec.subtract(damagerVec);

                    damagee.setVelocity(pushDir.normalize().multiply(5));
                    event.setCancelled(true);
                } else if (new BlazeReapRelicType(context).isRelicItem(((Player) damager).getInventory().getItemInMainHand())) {
                    new BlazeReapRelicType(context).doBlaze((Player) damager, damagee.getLocation().getBlock());
                }
            }
        }
    }

    @EventHandler()
    public void onPlayerUseItem(PlayerInteractEvent playerInteractEvent) {
        ItemStack item = playerInteractEvent.getItem();

        if(new PushStickRelicType(context).isRelicItem(item)){
            playerInteractEvent.setCancelled(true);
        }
        if (new BlazeReapRelicType(context).isRelicItem(item)) {
            new BlazeReapRelicType(context).onUse(playerInteractEvent);

            playerInteractEvent.setCancelled(true);
        } else if (unheardBellRelicType.isRelicItem(item)) {
            unheardBellRelicType.onUse(playerInteractEvent);
            playerInteractEvent.setCancelled(true);

        } else if (new IncineratorRelicType(context).isRelicItem(item)) {
            new IncineratorRelicType(context).onUse(playerInteractEvent);
            playerInteractEvent.setCancelled(true);

        } else if (new GrapplingHookRelicType(context).isRelicItem(item)){
            new GrapplingHookRelicType(context).onUse(playerInteractEvent);
            playerInteractEvent.setCancelled(true);
        }
    }
}
