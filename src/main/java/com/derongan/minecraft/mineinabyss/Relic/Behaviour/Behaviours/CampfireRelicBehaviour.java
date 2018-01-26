package com.derongan.minecraft.mineinabyss.Relic.Behaviour.Behaviours;

import com.derongan.minecraft.mineinabyss.Relic.Behaviour.EntityHitRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.UseRelicBehaviour;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

public class CampfireRelicBehaviour implements EntityHitRelicBehaviour, UseRelicBehaviour {
    @Override
    public void onHit(EntityDamageByEntityEvent event) {
        //doBlaze((Player)event.getDamager(), event.getEntity().getLocation().getBlock());
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getBlockFace() == BlockFace.UP) {
            event.setCancelled(true);

            Player player = event.getPlayer();

            Block target = event.getClickedBlock();

            event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
            doPlaceCampfire(player, target);
        }
    }


    private void doPlaceCampfire(Player player, Block target) {
        ArmorStand as  = (ArmorStand) player.getWorld().spawnEntity(target.getLocation(), EntityType.ARMOR_STAND);
        as.setGravity(false);
        as.setArms(true);
        as.setItemInHand(new ItemStack(Material.DIAMOND_HOE, 1, (short) 10));
        as.setRightArmPose(new EulerAngle(-90, 0, 0));
    }
}
