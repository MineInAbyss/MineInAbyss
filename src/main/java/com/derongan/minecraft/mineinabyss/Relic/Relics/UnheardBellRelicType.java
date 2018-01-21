package com.derongan.minecraft.mineinabyss.Relic.Relics;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.List;

public class UnheardBellRelicType extends AbstractRelicType {
    public UnheardBellRelicType() {
        super(Material.DIAMOND_PICKAXE, 3);
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
    }

    @Override
    public String getName() {
        return "Unheard Bell";
    }

    @Override
    public List<String> getLore() {
        return Arrays.asList("ZA WARUDO... I mean abyss stuff");
    }
}
