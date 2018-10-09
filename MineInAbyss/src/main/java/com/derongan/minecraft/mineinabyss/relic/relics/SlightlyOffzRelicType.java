package com.derongan.minecraft.mineinabyss.relic.relics;

import com.derongan.minecraft.mineinabyss.relic.behaviour.RelicBehaviour;
import com.derongan.minecraft.mineinabyss.relic.RelicRarity;
import com.derongan.minecraft.mineinabyss.relic.behaviour.behaviours.campfire.CampfireRelicBehaviour;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public enum SlightlyOffzRelicType implements RelicType {
    CAMPFIRE(Material.WOOD_SPADE,
            3,
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
