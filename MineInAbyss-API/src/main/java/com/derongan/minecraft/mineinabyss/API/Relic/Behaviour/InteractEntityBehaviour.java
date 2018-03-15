package com.derongan.minecraft.mineinabyss.API.Relic.Behaviour;

import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public interface InteractEntityBehaviour extends RelicBehaviour {
    public void onInteractEntity(PlayerInteractEntityEvent event);
}
