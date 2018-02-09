package com.derongan.minecraft.mineinabyss.plugin.Relic;

import com.derongan.minecraft.mineinabyss.API.Relic.Behaviour.DecayableRelicBehaviour;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Behaviour.Behaviours.Campfire.CampfireTimerBehaviour;
import org.bukkit.scheduler.BukkitRunnable;

public class RelicDecayTask extends BukkitRunnable {
    private final int ticks;

    public RelicDecayTask(int ticks) {
        super();
        this.ticks = ticks;
    }

    @Override
    public void run() {
        for (DecayableRelicBehaviour.RelicInfo relicInfo : DecayableRelicBehaviour.registeredRelics.values()) {
            ((DecayableRelicBehaviour)relicInfo.relicType.getBehaviour()).onDecay(relicInfo, ticks);
        }
        for (CampfireTimerBehaviour.CampfireInfo campfireInfo : CampfireTimerBehaviour.registeredCampfires.values()) {
            ((CampfireTimerBehaviour)campfireInfo.relicType.getBehaviour()).doCook(campfireInfo, ticks);
        }
    }
}
