package com.derongan.minecraft.mineinabyss.API.Relic.Behaviour;

import org.bukkit.event.player.PlayerInteractEvent;

public interface UseRelicBehaviour extends RelicBehaviour {
    public void onUse(PlayerInteractEvent event);
}
