package com.derongan.minecraft.mineinabyss.plugin.Relic.Behaviour.Behaviours.Campfire;

import com.derongan.minecraft.mineinabyss.API.Relic.Behaviour.RelicBehaviour;
import com.derongan.minecraft.mineinabyss.API.Relic.Relics.RelicType;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

//todo fields in here and similar should probably lived in an abstract impl. We should make it so that all decayable is handled
//with shared logic (Block or not). Therefore cleanupable should have most of its logic stripped.

/**
 * A relic that decays over time when placed on the map.
 */
public interface CampfireTimerBehaviour extends RelicBehaviour {
    Map<UUID, CampfireInfo> registeredCampfires = new ConcurrentHashMap<>();


    static void registerCampfire(RelicType relicType , Entity entity, int maxCoal) {
        registeredCampfires.put(entity.getUniqueId(), new CampfireInfo(0, 0, maxCoal, relicType, entity));
    }

    static void setCookTime(int cookTime, Entity entity) {
        registeredCampfires.get(entity.getUniqueId()).cookTime = cookTime;
    }

    static void addBurnTime(int addCoal, Entity entity, Player p){
        CampfireInfo campfire = registeredCampfires.get(entity.getUniqueId());
        if(campfire.coalLeft <= campfire.maxCoal-addCoal) {
            ArmorStand as = (ArmorStand) entity;
            ItemStack is = as.getHelmet();

            is.setDurability((short) 2);
            as.setHelmet(is);

            if(campfire.coalLeft < 0){
                campfire.coalLeft = 0;
            }
            campfire.coalLeft += addCoal;

            p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
        }
    }

    static void stopBurning(Entity entity){
        registeredCampfires.remove(entity.getUniqueId());
    }

    void doCook(CampfireInfo campfireInfo, int ticks);

    class CampfireInfo {
        public int coalLeft;
        public int cookTime;
        public int maxCoal;
        public RelicType relicType;
        public Object entity;

        public CampfireInfo(int coalLeft, int cookTime, int maxCoal, RelicType relicType, Object entity) {
            this.coalLeft = coalLeft;
            this.cookTime = cookTime;
            this.maxCoal = maxCoal;
            this.relicType = relicType;
            this.entity = entity;
        }
    }
}
