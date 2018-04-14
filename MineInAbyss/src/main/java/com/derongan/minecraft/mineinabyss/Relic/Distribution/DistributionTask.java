package com.derongan.minecraft.mineinabyss.Relic.Distribution;

import com.derongan.minecraft.mineinabyss.Relic.Relics.LootableRelicType;
import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.util.TickUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class DistributionTask extends BukkitRunnable {
    private AbyssContext context;
    private LootableRelicType lootableRelicType;

    public DistributionTask(AbyssContext context) {
        this.context = context;
        lootableRelicType = new LootableRelicType();
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

    void spawnLootableRelic(Location location, RelicType relicType) {
        lootableRelicType.spawnLootableRelic(location, relicType, TickUtils.milisecondsToTicks(10000));
    }
}
