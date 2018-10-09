package com.derongan.minecraft.mineinabyss.relic.relics;

import com.derongan.minecraft.mineinabyss.relic.behaviour.behaviours.LootableRelicBehaviour;
import com.derongan.minecraft.mineinabyss.relic.behaviour.RelicBehaviour;
import com.derongan.minecraft.mineinabyss.relic.RelicRarity;
import org.bukkit.Material;

import java.util.List;

/**
 * A "relic" used for spawned loot
 */
public class LootableRelicType implements RelicType {
    LootableRelicBehaviour behaviour = new LootableRelicBehaviour();

    @Override
    public String getName() {
        return null;
    }

    @Override
    public List<String> getLore() {
        return null;
    }

    @Override
    public Material getMaterial() {
        return null;
    }

    //TODO maybe have a base relic type with no item info
    @Override
    public short getDurability() {
        return 0;
    }

    @Override
    public RelicBehaviour getBehaviour() {
        return behaviour;
    }

    @Override
    public RelicRarity getRarity() {
        return null;
    }
}
