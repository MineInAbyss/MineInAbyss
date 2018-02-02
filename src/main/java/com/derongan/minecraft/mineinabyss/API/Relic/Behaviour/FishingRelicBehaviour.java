package com.derongan.minecraft.mineinabyss.API.Relic.Behaviour;

import org.bukkit.event.player.PlayerFishEvent;

public interface FishingRelicBehaviour extends RelicBehaviour {
    public void onFish(PlayerFishEvent event);
}
