package com.derongan.minecraft.mineinabyss.Player;

import com.derongan.minecraft.mineinabyss.Ascension.Effect.AscensionEffect;
import com.derongan.minecraft.mineinabyss.World.Layer;
import org.bukkit.entity.Player;

import java.util.List;

public interface PlayerData {
    /**
     * Get the bukkit player for this data.
     * @return The player
     */
    Player getPlayer();

    /**
     * Get the Layer the player is on
     * @return The Layer the player is on
     */
    Layer getCurrentLayer();

    /**
     * Set the layer the player is on. Use with caution.
     * You are responsible for moving the player so data is not out of sync
     * @param currentLayer The layer to move the player to
     */
    void setCurrentLayer(Layer currentLayer);

    /**
     * Get if the player is affected by the curse of ascending in the abyss
     * @return True if the player is affected, false otherwise
     */
    boolean isAffectedByCurse();

    /**
     * Set whether the player is affected by the curse of ascending in the abyss
     * @param affectedByCurse Whether the player is affected
     */
    void setAffectedByCurse(boolean affectedByCurse);

    /**
     * Get if the player will ignore automatic movement between sections
     * @return True if the player is not teleported when nearing bottom or top of layer.
     */
    boolean isAnchored();

    /**
     * Set whether the player is anchored the player to a Section
     * @param anchored True if the player should not automatically change sections, false otherwise
     */
    void setAnchored(boolean anchored);

    /**
     * Get the current effects on this player
     * @return The mutable list of effects on this player.
     */
    List<AscensionEffect> getAscensionEffects();

    /**
     * Add an effect to the player
     * @param effect the effect to add
     */
    void addAscensionEffect(AscensionEffect effect);

    /**
     * Get the distance the player has ascended since last being affected by the curse
     * @return The distance in meters
     */
    double getDistanceAscended();

    /**
     * Set the distance the player has ascended.
     * TODO should this be exposed like this?
     */
    void setDistanceAscended(double distanceAscended);
}
