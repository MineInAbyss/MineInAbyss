package com.derongan.minecraft.mineinabyss.relic.behaviour;

import org.bukkit.event.entity.EntityDamageByEntityEvent;

public interface EntityHitRelicBehaviour extends RelicBehaviour {
    public void onHit(EntityDamageByEntityEvent event);
}
