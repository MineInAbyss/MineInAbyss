package com.derongan.minecraft.mineinabyss.Relic;

import com.derongan.minecraft.mineinabyss.Relic.Behaviour.DecayableRelicBehaviour;
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
    }
}
