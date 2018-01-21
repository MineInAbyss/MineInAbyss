package com.derongan.minecraft.mineinabyss.Relic.Relics;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.List;

public class PushStickRelicType extends AbstractRelicType {
    public PushStickRelicType() {
        super(Material.DIAMOND_PICKAXE, 2);
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
    }

    @Override
    public String getName() {
        return "Push stick";
    }

    @Override
    public List<String> getLore() {
        return Arrays.asList("A pushing stick");
    }
}
