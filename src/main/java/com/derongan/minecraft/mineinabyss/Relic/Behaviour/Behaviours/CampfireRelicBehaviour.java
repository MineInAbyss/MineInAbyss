package com.derongan.minecraft.mineinabyss.Relic.Behaviour.Behaviours;

import com.derongan.minecraft.mineinabyss.Relic.Behaviour.UseRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.Relic.Relics.SlightlyOffzRelicType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.EulerAngle;

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
        RelicType type = SlightlyOffzRelicType.CAMPFIRE;

        ArmorStand as = type.getAndPlaceItem(target.getLocation());
        as.addScoreboardTag("Campfire");
        as.setRightArmPose(new EulerAngle(Math.toRadians(180), Math.toRadians(-75), Math.toRadians(90)));
    }
}
