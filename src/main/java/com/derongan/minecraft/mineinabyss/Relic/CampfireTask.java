package com.derongan.minecraft.mineinabyss.Relic;

import com.derongan.minecraft.mineinabyss.Relic.Behaviour.CampfireTimerBehaviour;
import org.bukkit.scheduler.BukkitRunnable;

public class CampfireTask extends BukkitRunnable {
    private final int ticks;

    public CampfireTask(int ticks) {
        super();
        this.ticks = ticks;
    }

    @Override
    public void run() {
        for (CampfireTimerBehaviour.CampfireInfo campfireInfo : CampfireTimerBehaviour.registeredCampfires.values()) {
            ((CampfireTimerBehaviour)campfireInfo.relicType.getBehaviour()).doCook(campfireInfo, ticks);
        }
    }
}
