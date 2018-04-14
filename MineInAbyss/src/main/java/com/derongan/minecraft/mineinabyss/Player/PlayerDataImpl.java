package com.derongan.minecraft.mineinabyss.Player;

import com.derongan.minecraft.mineinabyss.Ascension.Effect.AscensionEffect;
import com.derongan.minecraft.mineinabyss.World.Layer;
import com.derongan.minecraft.mineinabyss.World.Section;
import com.google.common.annotations.VisibleForTesting;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class PlayerDataImpl implements PlayerData {
    private Layer currentLayer;
    private Section currentSection;

    private boolean affectedByCurse;
    private boolean anchored;

    @VisibleForTesting
    Player player;

    public PlayerDataImpl(Player player) {
        this.player = player;
    }

    private List<AscensionEffect> effects;

    @Override
    public Layer getCurrentLayer() {
        return currentLayer;
    }

    @Override
    public void setCurrentLayer(Layer currentLayer) {
        this.currentLayer = currentLayer;
    }

    @Override
    public Section getCurrentSection() {
        return currentSection;
    }

    @Override
    public void setCurrentSection(Section currentSection) {
        this.currentSection = currentSection;
    }

    @Override
    public boolean isAffectedByCurse() {
        return affectedByCurse;
    }

    @Override
    public void setAffectedByCurse(boolean affectedByCurse) {
        this.affectedByCurse = affectedByCurse;
    }

    @Override
    public boolean isAnchored() {
        return anchored;
    }

    @Override
    public void setAnchored(boolean anchored) {
        this.anchored = anchored;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public List<AscensionEffect> getAscensionEffects() {
        return effects;
    }

    @Override
    public void addAscensionEffect(AscensionEffect effect) {
        effects.add(effect);
    }

    public static PlayerDataImpl valueOf(Map<String, Object> map){
        Player player = Bukkit.getPlayer((UUID)map.get("uuid"));
        boolean affected = (boolean) map.get("affected");
        boolean anchored = (boolean) map.get("anchored");

        PlayerDataImpl playerData = new PlayerDataImpl(player);

        //TODO think about this. We initialize in a way that only we can, and
        //dont expose ways to mess this object up.
        playerData.player = player;
        playerData.setAffectedByCurse(affected);
        playerData.setAnchored(anchored);

        return playerData;
    }
}
