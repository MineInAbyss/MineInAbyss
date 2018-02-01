package com.derongan.minecraft.mineinabyss.Relic.Behaviour;

import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

//todo fields in here and similar should probably lived in an abstract impl. We should make it so that all decayable is handled
//with shared logic (Block or not). Therefore cleanupable should have most of its logic stripped.

/**
 * A relic that decays over time when placed on the map.
 */
public interface DecayableRelicBehaviour extends RelicBehaviour {
    Map<UUID, RelicInfo> registeredRelics = new ConcurrentHashMap<>();


    default void registerRelic(Entity entity, int lifetime, RelicType relicType) {
        registeredRelics.put(entity.getUniqueId(), new RelicInfo(lifetime, 0, entity, relicType));
    }

    void onDecay(RelicInfo relicInfo, int ticks);

    class RelicInfo {
        public int lifeTime;
        public int lived;
        public Object entity;
        public RelicType relicType;

        public RelicInfo(int lifeTime, int lived, Object entity, RelicType relicType) {
            this.lifeTime = lifeTime;
            this.lived = lived;
            this.entity = entity;
            this.relicType = relicType;
        }
    }
}
