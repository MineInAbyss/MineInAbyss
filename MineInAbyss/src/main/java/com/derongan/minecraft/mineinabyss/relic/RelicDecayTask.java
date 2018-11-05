package com.derongan.minecraft.mineinabyss.relic;

import com.derongan.minecraft.mineinabyss.relic.behaviour.CooldownRelicBehaviour;
import com.derongan.minecraft.mineinabyss.relic.behaviour.DecayableRelicBehaviour;
import com.derongan.minecraft.mineinabyss.relic.behaviour.behaviours.campfire.CampfireTimerBehaviour;
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
            ((DecayableRelicBehaviour) relicInfo.relicType.getBehaviour()).onDecay(relicInfo, ticks);
        }
        for (CampfireTimerBehaviour.CampfireInfo campfireInfo : CampfireTimerBehaviour.registeredCampfires.values()) {
            ((CampfireTimerBehaviour) campfireInfo.relicType.getBehaviour()).doCook(campfireInfo, ticks);
        }
        for (CooldownRelicBehaviour.CooldownInfo cooldownInfo : CooldownRelicBehaviour.registeredCooldowns.values()) {
            ((CooldownRelicBehaviour) cooldownInfo.relicType.getBehaviour()).onCooldown(cooldownInfo, ticks);
        }
    }
}