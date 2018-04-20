package com.derongan.minecraft.mineinabyss.Relic.Behaviour;

import org.bukkit.event.player.PlayerInteractEvent;

public interface UseRelicBehaviour extends RelicBehaviour {
    public void onUse(PlayerInteractEvent event);
}
