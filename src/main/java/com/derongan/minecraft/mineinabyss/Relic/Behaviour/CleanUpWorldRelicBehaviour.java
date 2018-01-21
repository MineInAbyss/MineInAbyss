package com.derongan.minecraft.mineinabyss.Relic.Behaviour;

import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public interface CleanUpWorldRelicBehaviour extends RelicBehaviour {
    Map<Location, Runnable> registeredLocations = new HashMap<>();

    default void registerCleanupAction(Location location, Runnable runnable) {
        registeredLocations.put(location, runnable);
    }

    static void cleanUp(Location location) {
        Runnable runnable = registeredLocations.get(location);

        if (runnable != null) {
            runnable.run();
        }
    }

    void setRelicType(RelicType type);
}
