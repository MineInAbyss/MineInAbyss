package com.derongan.minecraft.mineinabyss.Relic.Relics;

import com.derongan.minecraft.mineinabyss.Relic.Behaviour.Behaviours.*;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.CleanUpWorldRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.RelicBehaviour;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public enum SlightlyOffzRelicType implements RelicType {
    OFFZERS(Material.COOKED_BEEF,
            0,
            new BlazeReapRelicBehaviour(),
            "Offzers",
            Arrays.asList("Something seems a little", "Offz about this food...")
    );

    private final Material material;
    private final short durability;
    private final String name;
    private final List<String> lore;
    private final RelicBehaviour behaviour;

    SlightlyOffzRelicType(Material material, long durability, RelicBehaviour behaviour, String name, List<String> lore) {
        this.durability = (short) durability;
        this.material = material;
        this.behaviour = behaviour;
        this.name = name;
        this.lore = lore;

        if(behaviour instanceof CleanUpWorldRelicBehaviour)
            ((CleanUpWorldRelicBehaviour) behaviour).setRelicType(this);
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getLore() {
        return lore;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public short getDurability() {
        return durability;
    }

    @Override
    public RelicBehaviour getBehaviour() {
        return behaviour;
    }
}
