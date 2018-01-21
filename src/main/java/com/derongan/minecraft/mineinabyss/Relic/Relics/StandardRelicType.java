package com.derongan.minecraft.mineinabyss.Relic.Relics;

import com.derongan.minecraft.mineinabyss.Relic.Behaviour.Behaviours.BlazeReapRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.Behaviours.IncineratorRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.Behaviours.PushingRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.Behaviours.TimeStopRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.RelicBehaviour;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public enum StandardRelicType implements RelicType {
    BLAZE_REAP(Material.DIAMOND_PICKAXE,
            0,
            new BlazeReapRelicBehaviour(),
            "Blaze Reap",
            Arrays.asList("An abnormally large pickaxe", "that contains Everlasting Gunpowder.")
    ),
    INCINERATOR(Material.FLINT_AND_STEEL,
            0,
            new IncineratorRelicBehaviour(),
            "Incinerator",
            Arrays.asList("A severed robotic arm.", "You can fiddle with it", "and things explode.")
    ),
    PUSHER(Material.WOOD_SPADE,
            1,
            new PushingRelicBehaviour(),
            "Push Stick",
            Arrays.asList("It pushes things.")
    ),
    UNHEARD_BELL(Material.WATCH,
            0,
            new TimeStopRelicBehaviour(),
            "Unheard Bell",
            Arrays.asList("What does it do?", "It is rumored to stop time.")
    );

    private final Material material;
    private final short durability;
    private final String name;
    private final List<String> lore;
    private final RelicBehaviour behaviour;

    StandardRelicType(Material material, long durability, RelicBehaviour behaviour, String name, List<String> lore) {
        this.durability = (short) durability;
        this.material = material;
        this.behaviour = behaviour;
        this.name = name;
        this.lore = lore;
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
