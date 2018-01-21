package com.derongan.minecraft.mineinabyss.Relic.Behaviour;

import org.bukkit.event.entity.EntityDamageByEntityEvent;

public interface EntityHitRelicBehaviour extends RelicBehaviour {
    public void onHit(EntityDamageByEntityEvent event);
}
