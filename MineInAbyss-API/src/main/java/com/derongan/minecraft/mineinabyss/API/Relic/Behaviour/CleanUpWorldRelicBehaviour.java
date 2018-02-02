package com.derongan.minecraft.mineinabyss.API.Relic.Behaviour;

import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


//todo this is a badly done behaviour type
/**
 * Relics that have locations and blocks to cleanup
 */
public interface CleanUpWorldRelicBehaviour extends RelicBehaviour {
    Map<Location, Runnable> registeredLocations = new ConcurrentHashMap<>();

    default void registerCleanupAction(Location location, Runnable runnable) {
        registeredLocations.put(location, runnable);
    }

    static void cleanUp(Location location) {
        Runnable runnable = registeredLocations.get(location);

        if (runnable != null) {
            runnable.run();
        }
    }
}
