package com.derongan.minecraft.mineinabyss.plugin.Relic.Behaviour.Behaviours.Campfire;

import com.derongan.minecraft.mineinabyss.API.Relic.Behaviour.ArmorStandBehaviour;
import com.derongan.minecraft.mineinabyss.API.Relic.Behaviour.PlacableRelicBehaviour;
import com.derongan.minecraft.mineinabyss.API.Relic.Behaviour.UseRelicBehaviour;
import com.derongan.minecraft.mineinabyss.API.Relic.Relics.RelicType;
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

import java.util.Arrays;
import java.util.List;

public class CampfireRelicBehaviour implements CampfireTimerBehaviour, UseRelicBehaviour, ArmorStandBehaviour {

    RelicType type;
    @Override
    public void onUse(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getBlockFace() == BlockFace.UP) {
            event.setCancelled(true);

            Player player = event.getPlayer();
            Block target = event.getClickedBlock();
            Material ma = target.getType();
            List allowedPlacements  = Arrays.asList(Material.GRASS, Material.STONE, Material.DIRT);

            if (allowedPlacements.contains(ma)) {
                player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                doPlaceCampfire(target);
            }
        }
    }

    private void doPlaceCampfire(Block target) {
        ArmorStand as = PlacableRelicBehaviour.getAndPlaceItem(type, target.getLocation(), .5, 0.6, .5, 180, -75, 90);
        as.addScoreboardTag("Campfire");
        ArmorStandBehaviour.registeredRelics.put(as.getUniqueId(), type);
        CampfireTimerBehaviour.registerCampfire(type, as, 1800);

        as.setRightArmPose(new EulerAngle(Math.toRadians(180), Math.toRadians(-75), Math.toRadians(90)));


    }


    @Override
    public void onPlayerInteractEntity(PlayerInteractAtEntityEvent e) {
        Player p = e.getPlayer();
        ArmorStand as = (ArmorStand) e.getRightClicked();
        Material ma = p.getInventory().getItemInMainHand().getType();
        List foodItems = Arrays.asList(Material.RABBIT, Material.RAW_CHICKEN, Material.RAW_FISH, Material.RAW_BEEF, Material.PORK);

        if (ma.equals(Material.COAL)) { //If player is holding coal
            CampfireTimerBehaviour.addBurnTime(600, as, p);

            e.setCancelled(true);
        } else if (!as.getItemInHand().getType().equals(Material.AIR)) { //If ArmorStand item isn't empty (i.e has food)
            ItemStack is = as.getItemInHand();
            is.setAmount(1);

            as.setItemInHand(new ItemStack(Material.AIR));
            p.getInventory().addItem(is);

            e.setCancelled(true);
        } else if (foodItems.contains(ma)) { //If player is holding a food item
            CampfireTimerBehaviour.setCookTime(200, as);

            as.setItemInHand(p.getInventory().getItemInMainHand());
            p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);

            e.setCancelled(true);
        } else {
            p.getInventory().addItem(type.getItem());
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
    @Override
    public void setRelicType(RelicType type){
        this.type = type;
    }
}
