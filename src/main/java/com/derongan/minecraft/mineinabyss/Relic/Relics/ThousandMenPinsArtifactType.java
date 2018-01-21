package com.derongan.minecraft.mineinabyss.Relic.Relics;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.List;

public class ThousandMenPinsArtifactType extends AbstractRelicType {
    public ThousandMenPinsArtifactType(AbyssContext context) {
        super(Material.DIAMOND_PICKAXE, 4, context);
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
    }

    @Override
    public String getName() {
        return "Thousand-Men Pin";
    }

    @Override
    public List<String> getLore() {
        return Arrays.asList("Each pin is said to bestow the strength of a thousand men");
    }
}
