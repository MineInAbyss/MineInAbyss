package com.derongan.minecraft.mineinabyss.plugin.Relic.Behaviour.Behaviours;

import com.derongan.minecraft.mineinabyss.API.Relic.Behaviour.ArmorStandBehaviour;
import com.derongan.minecraft.mineinabyss.API.Relic.Behaviour.UseRelicBehaviour;
import com.derongan.minecraft.mineinabyss.API.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Relics.SlightlyOffzRelicType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

public class CampfireRelicBehaviour implements CampfireTimerBehaviour, UseRelicBehaviour, ArmorStandBehaviour {
    @Override
    public void onUse(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getBlockFace() == BlockFace.UP) {
            event.setCancelled(true);

            Player player = event.getPlayer();

            Block target = event.getClickedBlock();

            Material ma = target.getType();

            if (ma.equals(Material.GRASS) || ma.equals(Material.STONE)) {
                event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                doPlaceCampfire(target);
            }
        }
    }

    private void doPlaceCampfire(Block target) {
        RelicType type = SlightlyOffzRelicType.CAMPFIRE;

        ArmorStand as = type.getAndPlaceItem(target.getLocation());//TODO Create a new time called PlaceableRelicBehaviour
        as.addScoreboardTag("Campfire");
        ArmorStandBehaviour.registeredRelics.put(as.getUniqueId(), type);
        CampfireTimerBehaviour.registerCampfire(type, as);
        as.setRightArmPose(new EulerAngle(Math.toRadians(180), Math.toRadians(-75), Math.toRadians(90)));


    }


    @Override
    public void onPlayerInteractEntity(PlayerInteractAtEntityEvent e) {
        Player p = e.getPlayer();
        ArmorStand as = (ArmorStand) e.getRightClicked();
        Material ma = p.getInventory().getItemInMainHand().getType();
        if (ma.equals(Material.COAL)) {
            p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
            CampfireTimerBehaviour.addBurnTime(200, as);
            e.setCancelled(true);
        } else if (!as.getItemInHand().getType().equals(Material.AIR)) {
            ItemStack is = as.getItemInHand();
            is.setAmount(1);
            p.getInventory().addItem(is);
            as.setItemInHand(new ItemStack(Material.AIR));
            e.setCancelled(true);
        } else if (ma.equals(Material.RABBIT) || ma.equals(Material.RAW_CHICKEN) || ma.equals(Material.RAW_FISH) || ma.equals(Material.RAW_BEEF) || ma.equals(Material.PORK)) {
            //if(as.getHelmet().equals(CampfireTimerBehaviour.registeredCampfires.containsKey(as.getUniqueId()))) {
                CampfireTimerBehaviour.setCookTime(100, as);
            //}
            as.setItemInHand(p.getInventory().getItemInMainHand());
            p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
            e.setCancelled(true);
        }else {
            p.getInventory().addItem(SlightlyOffzRelicType.CAMPFIRE.getItem());
            CampfireTimerBehaviour.stopBurning(as);
            as.remove();
            e.setCancelled(true);
        }
    }

    @Override
    public void doCook(CampfireTimerBehaviour.CampfireInfo toCook, int ticks) {

        ArmorStand as = (ArmorStand) toCook.entity;

        if(toCook.coalLeft <= 0){
            ItemStack is = as.getHelmet();
            is.setDurability((short) 3);
            as.setHelmet(is);
        }else {
            toCook.coalLeft -= ticks;
            toCook.cookTime -= ticks;
            if (!as.getItemInHand().getType().equals(Material.AIR)) {
                if (toCook.cookTime <= 0) {
                    Material hand = as.getItemInHand().getType();
                    ItemStack is = as.getItemInHand();
                    if (hand == Material.RABBIT) {
                        is.setType(Material.COOKED_RABBIT);
                    } else if (hand == Material.RAW_CHICKEN) {
                        is.setType(Material.COOKED_CHICKEN);
                    } else if (hand == Material.RAW_FISH) {
                        is.setType(Material.COOKED_FISH);
                    } else if (hand == Material.RAW_BEEF) {
                        is.setType(Material.COOKED_BEEF);
                    } else if (hand == Material.PORK) {
                        is.setType(Material.GRILLED_PORK);
                    }
                    as.setItemInHand(is);
                }
                as.getWorld().spawnParticle(Particle.SMOKE_NORMAL, as.getLocation().add(0.25, 1.6, 0), 1, 0, 0, 0, 0);
            }
        }
    }
}
