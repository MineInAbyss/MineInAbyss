package com.derongan.minecraft.mineinabyss.Relic.Behaviour.Behaviours;

import com.derongan.minecraft.mineinabyss.Relic.Behaviour.EntityHitRelicBehaviour;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class PushingRelicBehaviour implements EntityHitRelicBehaviour {
    @Override
    public void onHit(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damagee = event.getEntity();
        Vector damagerVec = damager.getLocation().toVector();
        Vector damageeVec = damagee.getLocation().toVector();

        // Vector pointing towards damagee
        Vector pushDir = damageeVec.subtract(damagerVec);
        pushDir.add(new Vector(0,.2,0)); // Force up for fun?

        damagee.setVelocity(pushDir.normalize().multiply(3));
        event.setCancelled(true);
    }
}
