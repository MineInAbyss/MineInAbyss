package com.derongan.minecraft.mineinabyss.relic.behaviour.behaviours;

import com.derongan.minecraft.mineinabyss.relic.behaviour.UseRelicBehaviour;
import com.derongan.minecraft.mineinabyss.relic.relics.RelicType;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class ThousandMenPinsRelicBehaviour implements UseRelicBehaviour{
    private RelicType myRelic;

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        player.getInventory().getItemInMainHand().setAmount(0);

        double oldHealthValue = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(oldHealthValue + 2);

        double oldDamageValue = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(oldDamageValue+1);
    }

    @Override
    public void setRelicType(RelicType type) {
        myRelic = type;
    }
}
