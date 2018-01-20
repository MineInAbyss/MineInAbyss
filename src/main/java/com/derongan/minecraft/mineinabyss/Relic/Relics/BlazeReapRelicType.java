package com.derongan.minecraft.mineinabyss.Relic.Relics;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class BlazeReapRelicType extends AbstractRelicType {
    public BlazeReapRelicType() {
        super(Material.DIAMOND_PICKAXE, 1);
    }

    @Override
    public void onUse() {

    }

    @Override
    public String getName() {
        return "Blaze Reap";
    }

    @Override
    public List<String> getLore() {
        return Arrays.asList("An abnormally large pickaxe that contains Everlasting Gunpowder");
    }
}
