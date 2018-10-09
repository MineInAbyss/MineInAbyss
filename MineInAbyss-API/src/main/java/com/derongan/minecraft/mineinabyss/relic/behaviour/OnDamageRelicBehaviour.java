package com.derongan.minecraft.mineinabyss.relic.behaviour;

import org.bukkit.event.entity.EntityDamageEvent;

public interface OnDamageRelicBehaviour extends RelicBehaviour{
    void onDamage(EntityDamageEvent e);
}
