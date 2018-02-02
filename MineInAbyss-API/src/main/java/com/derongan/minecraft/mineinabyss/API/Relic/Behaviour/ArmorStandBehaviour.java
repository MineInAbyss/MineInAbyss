package com.derongan.minecraft.mineinabyss.API.Relic.Behaviour;

import com.derongan.minecraft.mineinabyss.API.Relic.Relics.RelicType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Behaviour that fires when a registered armorstand is interacted with
 */
public interface ArmorStandBehaviour {
    Map<UUID, RelicType> registeredRelics = new HashMap<>();

    default void registerRelic(UUID uuid, RelicType type) {
        registeredRelics.put(uuid, type);
    }
    void onManipulateArmorStand(PlayerArmorStandManipulateEvent event);
}
