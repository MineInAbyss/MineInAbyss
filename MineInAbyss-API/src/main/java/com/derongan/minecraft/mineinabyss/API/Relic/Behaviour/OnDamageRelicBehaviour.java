package com.derongan.minecraft.mineinabyss.API.Relic.Behaviour;

import org.bukkit.event.entity.EntityDamageEvent;

public interface OnDamageRelicBehaviour extends RelicBehaviour{
    void onDamage(EntityDamageEvent e);
}
