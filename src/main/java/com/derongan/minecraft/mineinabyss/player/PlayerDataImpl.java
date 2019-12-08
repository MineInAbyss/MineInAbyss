package com.derongan.minecraft.mineinabyss.player;

import com.derongan.minecraft.mineinabyss.ascension.effect.AscensionEffect;
import com.derongan.minecraft.mineinabyss.whistles.WhistleType;
import com.derongan.minecraft.mineinabyss.world.Layer;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.List;


public class PlayerDataImpl implements PlayerData {
    private Layer currentLayer;

    private boolean affectedByCurse;
    private boolean anchored;
    private boolean ingame = false;

    private double distanceAscended;
    private double exp;
    private double expOnDescent;

    private Player player;
    private WhistleType whistle = WhistleType.BELL;
    private Date descentDate;
    private List<AscensionEffect> effects;

    public PlayerDataImpl(Player player) {
        this.player = player;
        this.affectedByCurse = true;
    }

    @Override
    public double getExpOnDescent() {
        return expOnDescent;
    }

    @Override
    public void setExpOnDescent(double expOnDescent) {
        this.expOnDescent = expOnDescent;
    }

    @Override
    public Date getDescentDate() {
        return descentDate;
    }

    @Override
    public void setDescentDate(Date date) {
        descentDate = date;
    }

    @Override
    public boolean isIngame() {
        return ingame;
    }

    @Override
    public void setIngame(boolean ingame) {
        this.ingame = ingame;
    }

    @Override
    public Layer getCurrentLayer() {
        return currentLayer;
    }

    @Override
    public void setCurrentLayer(Layer currentLayer) {
        this.currentLayer = currentLayer;
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

    @Override
    public double getDistanceAscended() {
        return distanceAscended;
    }

    @Override
    public void setDistanceAscended(double distanceAscended) {
        this.distanceAscended = distanceAscended;
    }

    @Override
    public WhistleType getWhistle() {
        return whistle;
    }

    @Override
    public void setWhistle(WhistleType whistle) {
        this.whistle = whistle;
    }

    @Override
    public int getLevel() {
        return (int) exp / 10; //TODO write a proper formula
    }

    @Override
    public double getExp() {
        return exp;
    }

    @Override
    public void setExp(double exp) {
        this.exp = exp;
    }

    @Override
    public void addExp(double exp) {
        this.exp += exp;
    }
}
