package com.derongan.minecraft.mineinabyss.Relic.Relics;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.List;

public class GrapplingHookRelicType extends AbstractRelicType {
    public GrapplingHookRelicType(AbyssContext context) {
        super(Material.WOOD_SPADE, 1, context);
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Block target = event.getClickedBlock();

        player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.FISHING_HOOK);

        event.setCancelled(true);
    }

    @Override
    public String getName() {
        return "Grappling Hook";
    }

    @Override
    public List<String> getLore() {
        return Arrays.asList("This isn't really a relic", "...", "but it is useful");
    }
}
