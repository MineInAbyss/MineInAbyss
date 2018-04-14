package com.derongan.minecraft.mineinabyss.Relic.Behaviour.Behaviours;

import com.derongan.minecraft.mineinabyss.MineInAbyss;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.CleanUpWorldRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.UseRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.util.TickUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;

public class UnfurlLadderRelicBehaviour implements UseRelicBehaviour, CleanUpWorldRelicBehaviour {
    private final int lengthLimit;
    private RelicType myRelic;

    public UnfurlLadderRelicBehaviour() {
        this(16);
    }

    public UnfurlLadderRelicBehaviour(int lengthLimit) {
        this.lengthLimit = lengthLimit;
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onUse(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getBlockFace() != BlockFace.UP) {
            event.setCancelled(true);

            event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount()-1);

            BlockFace face = event.getBlockFace();
            Block current = event.getClickedBlock().getRelative(face);


            List<Block> modifiedBlocks = new ArrayList<>();

            Location first = current.getLocation();

            List<Integer> tasks = new ArrayList<>();

            BukkitScheduler scheduler = event.getPlayer().getServer().getScheduler();

            if(!current.getType().equals(Material.AIR))
                return;

            int lengthFallen = 0;
            do {
                final Block theBlock = current;
                modifiedBlocks.add(theBlock);
                tasks.add(scheduler.scheduleSyncDelayedTask(JavaPlugin.getPlugin(MineInAbyss.class), () -> {
                    theBlock.setTypeIdAndData(Material.LADDER.getId(), getData(face), false);
                }, lengthFallen * 2));
                current = current.getRelative(BlockFace.DOWN);
                lengthFallen++;
            }
            while (current != null && current.getType().equals(Material.AIR) && lengthFallen <= lengthLimit);

            Location last = current.getRelative(BlockFace.UP).getLocation();

            tasks.add(scheduler.scheduleSyncDelayedTask(JavaPlugin.getPlugin(MineInAbyss.class), ()->{
                registeredLocations.remove(first);
                registeredLocations.remove(last);
                modifiedBlocks.forEach(a -> a.setTypeIdAndData(Material.AIR.getId(), (byte) 0, false));

                Item item = last.getWorld().dropItem(last.add(.5, -.5, .5), myRelic.getItem());
                item.setVelocity(item.getVelocity().zero());

                tasks.forEach(scheduler::cancelTask);
            }, TickUtils.milisecondsToTicks(1000*60*15)));

            registerCleanupAction(first, () -> {
                registeredLocations.remove(first);
                registeredLocations.remove(last);
                modifiedBlocks.forEach(a -> a.setTypeIdAndData(Material.AIR.getId(), (byte) 0, false));

                Item item = first.getWorld().dropItem(first.add(.5, 0, .5), myRelic.getItem());
                item.setVelocity(item.getVelocity().zero());

                tasks.forEach(scheduler::cancelTask);
            });

            registerCleanupAction(last, () -> {
                registeredLocations.remove(first);
                registeredLocations.remove(last);
                modifiedBlocks.forEach(a -> a.setTypeIdAndData(Material.AIR.getId(), (byte) 0, false));

                Item item = last.getWorld().dropItem(last.add(.5, -.5, .5), myRelic.getItem());
                item.setVelocity(item.getVelocity().zero());

                tasks.forEach(scheduler::cancelTask);
            });
        }
    }

    private byte getData(BlockFace face) {
        switch (face) {
            case NORTH:
                return (byte) 2;
            case SOUTH:
                return (byte) 3;
            case WEST:
                return (byte) 4;
            case EAST:
                return (byte) 5;
            default:
                return (byte) 0;
        }
    }

    @Override
    public void setRelicType(RelicType type) {
        myRelic = type;
    }
}
