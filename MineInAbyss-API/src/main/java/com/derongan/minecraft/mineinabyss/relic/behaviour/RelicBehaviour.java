package com.derongan.minecraft.mineinabyss.relic.behaviour;

import com.derongan.minecraft.mineinabyss.relic.relics.RelicType;

public interface RelicBehaviour {
    default void setRelicType(RelicType type){}
}
