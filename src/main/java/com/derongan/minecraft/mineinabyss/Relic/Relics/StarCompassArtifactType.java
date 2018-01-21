package com.derongan.minecraft.mineinabyss.Relic.Relics;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.List;

public class StarCompassArtifactType extends AbstractRelicType {
    public StarCompassArtifactType(AbyssContext context) {
        super(Material.COMPASS, 0, context);
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
    }

    @Override
    public String getName() {
        return "Star Compass";
    }

    @Override
    public List<String> getLore() {
        return Arrays.asList("This shit is broken -_-");
    }


}
