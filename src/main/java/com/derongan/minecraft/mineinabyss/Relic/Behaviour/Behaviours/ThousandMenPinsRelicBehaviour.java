package com.derongan.minecraft.mineinabyss.Relic.Behaviour.Behaviours;

import com.derongan.minecraft.mineinabyss.Relic.Behaviour.UseRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
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
