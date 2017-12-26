package com.derongan.minecraft.madeinabyss.Ascension;

import com.derongan.minecraft.madeinabyss.Ascension.Effect.Effects.AscensionEffect;
import com.derongan.minecraft.madeinabyss.Layer.Layer;
import com.google.common.collect.ImmutableSet;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class AscensionData {
    private double distanceMovedUp;

    private Set<AscensionEffect> currentEffects = new HashSet<>();

    private Layer currentLayer;

    private AscensionData(double distanceMovedUp) {
        this.setDistanceMovedUp(distanceMovedUp);
    }

    public AscensionData() {
        this(0);
    }

    public double getDistanceMovedUp() {
        return distanceMovedUp;
    }

    public void setDistanceMovedUp(double distanceMovedUp) {
        this.distanceMovedUp = distanceMovedUp;
    }

    public void changeDistanceMovedUp(double distanceMovedUp){
        this.setDistanceMovedUp(Math.max(this.distanceMovedUp + distanceMovedUp,0));
    }

    public ImmutableSet<AscensionEffect> getCurrentEffects(){
        return ImmutableSet.copyOf(currentEffects);
    }

    public void applyEffect(AscensionEffect newEffect){
        Class<?> clazz = newEffect.getClass();
        Optional<AscensionEffect> oldEffect = currentEffects.stream().filter(a->a.getClass().equals(clazz)).findFirst();

        if(oldEffect.isPresent()){
            oldEffect.get().setRemainingTicks(Math.max(oldEffect.get().getRemainingTicks(), newEffect.getRemainingTicks()));
        } else {
            currentEffects.add(newEffect);
        }
    }

    public void removeFinishedEffects(Player player){
        currentEffects.forEach(effect->{if(effect.isDone())effect.cleanUp(player);});
        currentEffects.removeIf(AscensionEffect::isDone);
    }

    public void clearEffects(Player player){
        currentEffects.forEach(effect->effect.cleanUp(player));
        currentEffects.clear();
    }

    public Layer getCurrentLayer() {
        return currentLayer;
    }

    public void setCurrentLayer(Layer currentLayer) {
        this.currentLayer = currentLayer;
    }
}
