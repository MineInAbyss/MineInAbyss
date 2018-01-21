package com.derongan.minecraft.mineinabyss.Relic.Relics;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.TickUtils;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UnheardBellRelicType extends AbstractRelicType {
    public UnheardBellRelicType(AbyssContext context) {
        super(Material.DIAMOND_PICKAXE, 3, context);
    }

    public Set<LivingEntity> freezeList;
    public boolean frozen = false;

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Set<LivingEntity> others = player.getWorld().getLivingEntities().stream().filter(a->a!=player).collect(Collectors.toSet());


        freezeList = others;


        others.forEach(a->{
            if(a != player) {
                a.setGravity(false);
                a.setVelocity(new Vector(0, 0, 0));
                a.setAI(false);

                if(a instanceof  Player){
                    ((Player) a).setFlySpeed(0);
                    ((Player) a).setWalkSpeed(0);
                }
            }
        });

        frozen = true;

        player.getServer().getScheduler().scheduleSyncDelayedTask(context.getPlugin(), ()->{
            for (LivingEntity other : others) {
                other.setGravity(true);
                other.setAI(true);

                if(other instanceof  Player){
                    ((Player) other).setFlySpeed(.2f);
                    ((Player) other).setWalkSpeed(.2f);
                }
            }
            frozen = false;
        }, TickUtils.milisecondsToTicks(5000));
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
