package com.derongan.minecraft.mineinabyss.Relic.Behaviour;

import org.bukkit.event.player.PlayerInteractEntityEvent;

public interface InteractEntityBehaviour extends RelicBehaviour {
    public void onInteractEntity(PlayerInteractEntityEvent event);
}
