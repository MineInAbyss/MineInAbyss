package com.derongan.minecraft.mineinabyss.relic.behaviour;

import org.bukkit.event.player.PlayerItemConsumeEvent;

public interface ConsumeRelicBehaviour extends RelicBehaviour {
    public void onConsume(PlayerItemConsumeEvent event);
}
