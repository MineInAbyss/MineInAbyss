package com.derongan.minecraft.mineinabyss.Relic.Behaviour.Behaviours;

import com.derongan.minecraft.mineinabyss.Relic.Behaviour.EntityHitRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.UseRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Relics.SlightlyOffzRelicType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class CampfireRelicBehaviour implements UseRelicBehaviour {
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
        ArmorStand as  = (ArmorStand) player.getWorld().spawnEntity(target.getLocation().add(0.45,-1.3,0.45), EntityType.ARMOR_STAND);
        as.setGravity(false);
        as.setVisible(false);
        as.setCustomName("Campfire");

        ItemStack is = new ItemStack(Material.DIAMOND_HOE, 1, (short) 2);
        ItemMeta meta = is.getItemMeta();
        meta.setUnbreakable(true);
        is.setItemMeta(meta);
        as.setHelmet(is);
    }
}
