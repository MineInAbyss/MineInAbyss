package com.derongan.minecraft.mineinabyss.relic.behaviour;

import org.bukkit.event.player.PlayerInteractEvent;

public interface UseRelicBehaviour extends RelicBehaviour {
    public void onUse(PlayerInteractEvent event);
}
