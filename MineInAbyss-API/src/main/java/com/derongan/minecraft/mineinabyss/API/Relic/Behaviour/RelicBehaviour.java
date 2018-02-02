package com.derongan.minecraft.mineinabyss.API.Relic.Behaviour;

import com.derongan.minecraft.mineinabyss.API.Relic.Relics.RelicType;

public interface RelicBehaviour {
    default void setRelicType(RelicType type){}
}
