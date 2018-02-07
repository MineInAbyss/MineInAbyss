package com.derongan.minecraft.mineinabyss.plugin.Relic.Relics;

import com.derongan.minecraft.mineinabyss.API.Relic.Behaviour.RelicBehaviour;
import com.derongan.minecraft.mineinabyss.API.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.API.Relic.RelicRarity;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Behaviour.Behaviours.*;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public enum StandardRelicType implements RelicType {
    BLAZE_REAP(Material.DIAMOND_PICKAXE,
            0,
            new BlazeReapRelicBehaviour(),
            "Blaze Reap",
            Arrays.asList("An abnormally large pickaxe", "that contains Everlasting Gunpowder."),
            RelicRarity.FIRST_GRADE
    ),
    INCINERATOR(Material.FLINT_AND_STEEL,
            0,
            new IncineratorRelicBehaviour(),
            "Incinerator",
            Arrays.asList("A severed robotic arm.", "You can fiddle with it", "and things explode."),
            RelicRarity.SPECIAL_GRADE
    ),
    PUSHER(Material.WOOD_SPADE,
            1,
            new PushingRelicBehaviour(),
            "Push Stick",
            Arrays.asList("It pushes things."),
            RelicRarity.SECOND_GRADE
    ),
    UNHEARD_BELL(Material.WATCH,
            0,
            new TimeStopRelicBehaviour(),
            "Unheard Bell",
            Arrays.asList("What does it do?", "It is rumored to stop time."),
            RelicRarity.SPECIAL_GRADE
    ),
    ROPE_LADDER(Material.LEASH,
            0,
            new UnfurlLadderRelicBehaviour(),
            "Rope Ladder",
            Arrays.asList("Places a Ladder"),
            RelicRarity.TOOL
    ),
    THOUSAND_MEN_PINS(Material.STONE_HOE,
            1,
            new ThousandMenPinsRelicBehaviour(),
            "Thousand-Men Pins",
            Arrays.asList("Each pin is said to bestow", "the strength of a thousand men", "", "Right click to consume"),
            RelicRarity.FIRST_GRADE
    ),
    SLAVE_STICK(Material.STICK,
            5,
            new SlaveRelicBehaviour(),
            "Slave Stick",
            Arrays.asList("Ha"),
            RelicRarity.SPECIAL_GRADE
    );

    private final Material material;
    private final short durability;
    private final String name;
    private final List<String> lore;
    private final RelicBehaviour behaviour;
    private final RelicRarity rarity;

    StandardRelicType(Material material, long durability, RelicBehaviour behaviour, String name, List<String> lore, RelicRarity rarity) {
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
