package com.derongan.minecraft.mineinabyss.relic.behaviour.behaviours.campfire;

import com.derongan.minecraft.mineinabyss.relic.behaviour.ArmorStandBehaviour;
import com.derongan.minecraft.mineinabyss.relic.behaviour.PlacableRelicBehaviour;
import com.derongan.minecraft.mineinabyss.relic.behaviour.UseRelicBehaviour;
import com.derongan.minecraft.mineinabyss.relic.relics.RelicType;
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
    List allowedPlacements  = Arrays.asList(Material.GRASS, Material.STONE, Material.DIRT);
    List foodItems = Arrays.asList(Material.RABBIT, Material.RAW_CHICKEN, Material.RAW_FISH, Material.RAW_BEEF, Material.PORK);

    @Override
    public void onUse(PlayerInteractEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getBlockFace() == BlockFace.UP && player.getInventory().getItemInMainHand().equals(type.getItem())) {
            Block target = event.getClickedBlock();
            Material ma = target.getType();

            if (this.allowedPlacements.contains(ma)) {
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

        if (ma.equals(Material.COAL)) { //If player is holding coal
            CampfireTimerBehaviour.addBurnTime(600, as, p);

            e.setCancelled(true);
        } else if (!as.getItemInHand().getType().equals(Material.AIR)) { //If ArmorStand item isn't empty (i.e has food)
            ItemStack is = as.getItemInHand();
            is.setAmount(1);

            as.setItemInHand(new ItemStack(Material.AIR));
            p.getInventory().addItem(is);

            e.setCancelled(true);
        } else if (this.foodItems.contains(ma)) { //If player is holding a food item
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
        }else {
            toCook.coalLeft -= ticks;
            toCook.cookTime -= ticks;
            if (!as.getItemInHand().getType().equals(Material.AIR)) {
                //TODO this is a hack to fix the annyoing sound issue
                if (toCook.cookTime <= 0 && toCook.cookTime > -1000) {
                    Material hand = as.getItemInHand().getType();
                    ItemStack is = as.getItemInHand();
                    switch(hand) {
                        case RABBIT:
                            is.setType(Material.COOKED_RABBIT);
                            break;
                        case RAW_CHICKEN:
                            is.setType(Material.COOKED_CHICKEN);
                            break;
                        case RAW_FISH:
                            is.setType(Material.COOKED_FISH);
                            break;
                        case RAW_BEEF:
                            is.setType(Material.COOKED_BEEF);
                            break;
                        case PORK:
                            is.setType(Material.GRILLED_PORK);
                            break;
                    }
                    as.setItemInHand(is);
                    toCook.cookTime = -10000;
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
