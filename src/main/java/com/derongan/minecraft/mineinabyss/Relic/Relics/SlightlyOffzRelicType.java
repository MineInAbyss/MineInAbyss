package com.derongan.minecraft.mineinabyss.Relic.Relics;

import com.derongan.minecraft.mineinabyss.Relic.Behaviour.Behaviours.*;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.RelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.RelicRarity;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public enum SlightlyOffzRelicType implements RelicType {
    CAMPFIRE(Material.DIAMOND_HOE,
            2,
            new CampfireRelicBehaviour(),
            "Campfire",
            Arrays.asList("Great for cooking food"),
            RelicRarity.TOOL
    );

    private final Material material;
    private final short durability;
    private final String name;
    private final List<String> lore;
    private final RelicBehaviour behaviour;
    private final RelicRarity rarity;

    SlightlyOffzRelicType(Material material, long durability, RelicBehaviour behaviour, String name, List<String> lore, RelicRarity rarity) {
        this.durability = (short) durability;
        this.material = material;
        this.behaviour = behaviour;
        this.name = name;
        this.lore = lore;
        this.rarity = rarity;

        behaviour.setRelicType(this);
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

    @Override
    public RelicRarity getRarity() {
        return rarity;
    }
}
