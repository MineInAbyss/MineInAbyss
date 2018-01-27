package com.derongan.minecraft.mineinabyss.Relic.Distribution;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Relic.RelicRarity;
import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.Random;

public class DistributionTask extends BukkitRunnable {
    AbyssContext context;

    public DistributionTask(AbyssContext context) {
        this.context = context;
    }

    @Override
    public void run() {
        for (Player p : context.getPlugin().getServer().getOnlinePlayers()) {
            if (p.getWorld().getName().equals("LayerOne"))
                doSomething(p.getLocation());
        }
    }

    public void doSomething(Location location) {
        Random random = new Random();
        Object[] relicTypes = RelicType.registeredRelics.values().toArray();
        RelicType type = (RelicType) relicTypes[random.nextInt(relicTypes.length)];
        spawnLootableRelic(location, type);
    }

    //Todo move elsewhere
    void spawnLootableRelic(Location location, RelicType relicType) {
        ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(location.add(.5, -1.4, .5).setDirection(new Vector(0, 0, 0)), EntityType.ARMOR_STAND);
        as.setGravity(false);
        as.setArms(true);
        as.setVisible(false);
        as.setCollidable(false);
        as.setItemInHand(relicType.getItem());
        as.setRightArmPose(new EulerAngle(-Math.PI / 2, -Math.PI / 2, 0));

        if (relicType.getRarity() == RelicRarity.SPECIAL_GRADE) {
            as.setCustomName(ChatColor.GRAY.toString() + ChatColor.MAGIC + relicType.getName());
        } else {
            as.setCustomName(ChatColor.GRAY + relicType.getName());
        }
        as.setCustomNameVisible(true);
        as.setInvulnerable(true);
    }
}
