package com.derongan.minecraft.mineinabyss.relic.behaviour;

import org.bukkit.event.player.PlayerInteractEntityEvent;

public interface InteractEntityBehaviour extends RelicBehaviour {
    public void onInteractEntity(PlayerInteractEntityEvent event);
}
