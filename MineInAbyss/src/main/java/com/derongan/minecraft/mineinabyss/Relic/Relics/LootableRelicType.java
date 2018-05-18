package com.derongan.minecraft.mineinabyss.Relic.Relics;

import com.derongan.minecraft.mineinabyss.Relic.Behaviour.DecayableRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.Behaviours.LootableRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.RelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.RelicRarity;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

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
