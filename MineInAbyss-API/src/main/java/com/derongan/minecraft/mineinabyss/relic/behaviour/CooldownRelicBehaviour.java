package com.derongan.minecraft.mineinabyss.relic.behaviour;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.derongan.minecraft.mineinabyss.relic.relics.RelicType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface CooldownRelicBehaviour extends RelicBehaviour {
    Map<MapCooldownInfo, CooldownInfo> registeredCooldowns = new HashMap<>();

    static void registerCooldown(Entity entity, int cooldown, RelicType relicType) {
        MapCooldownInfo mapCooldownInfo = new MapCooldownInfo(entity.getUniqueId(), relicType);
        registeredCooldowns.put(mapCooldownInfo, new CooldownInfo(cooldown, 0, entity, relicType, mapCooldownInfo));
    }

    default void onCooldown(CooldownInfo cooldownInfo, int ticks) {
        double cooldown = cooldownInfo.cooldown;
        Player player = (Player) cooldownInfo.entity;
        RelicType type = cooldownInfo.relicType;
        MapCooldownInfo mapCooldownInfo = cooldownInfo.mapCooldownInfo;

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        RelicType handRelicType = RelicType.getRegisteredRelicType(itemInHand);

        ChatMessageType messageType = ChatMessageType.ACTION_BAR;
        ComponentBuilder percentage = new ComponentBuilder("").bold(true); //empty action bar message

        if (cooldown <= 0) { //if cooldown is over
            registeredCooldowns.remove(mapCooldownInfo);
            cooledDown();
            if (itemInHand.equals(type.getItem()))
                player.spigot().sendMessage(messageType, percentage.create()); //clear action bar
        } else if (itemInHand.equals(type.getItem())) { //if player is holding the relic with cooldown
            for (int i = 0; i < 10; i++) { //draw a number of squares in green, depending on how close to complete the cooldown is
                if ((cooldown < 30) && (cooldown % 2 == 0)) //flash golden colour when cooldown about to complete
                    percentage.append("\u2B1B").color(ChatColor.GOLD);
                else if (i < 10 - cooldown / cooldownInfo.originalCooldown * 10)
                    percentage.append("\u2B1B").color(net.md_5.bungee.api.ChatColor.GREEN);
                else
                    percentage.append("\u2B1B").color(ChatColor.RED);
            }
            percentage.append(" " + Integer.toString(((int) cooldown - 1) / 20 + 1)).color(type.getRarity().getColor().asBungee()); //add seconds left
            player.spigot().sendMessage(messageType, percentage.create()); //display action bar
        } else if ((handRelicType == null) || (registeredCooldowns.get(new MapCooldownInfo(player.getUniqueId(), handRelicType)) == null)) //if player not holding the cooling-down relic, and no other cooldown is going on
            player.spigot().sendMessage(messageType, percentage.create()); //clear action bar
        cooldownInfo.cooldown -= ticks; //make cooldown go down
    }

    void cooledDown();

    class CooldownInfo {
        public double cooldown;
        public double originalCooldown;
        public int lived;
        public Object entity;
        public RelicType relicType;
        public MapCooldownInfo mapCooldownInfo;

        public CooldownInfo(double cooldown, int lived, Object entity, RelicType relicType, MapCooldownInfo mapCooldownInfo) {
            this.cooldown = cooldown;
            this.originalCooldown = cooldown;
            this.lived = lived;
            this.entity = entity;
            this.relicType = relicType;
            this.mapCooldownInfo = mapCooldownInfo;
        }
    }

    class MapCooldownInfo {
        public UUID uuid;
        public RelicType relicType;

        public MapCooldownInfo(UUID uuid, RelicType relicType) {
            this.uuid = uuid;
            this.relicType = relicType;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MapCooldownInfo)) {
                return false;
            }
            MapCooldownInfo other = (MapCooldownInfo) o;
            return (this.uuid.equals(other.uuid)) && (this.relicType.equals(other.relicType));
        }

        @Override
        public int hashCode() {
            return this.uuid.hashCode();
        }
    }
}