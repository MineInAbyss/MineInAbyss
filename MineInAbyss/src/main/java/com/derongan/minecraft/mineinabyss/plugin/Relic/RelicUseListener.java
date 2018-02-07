package com.derongan.minecraft.mineinabyss.plugin.Relic;

import com.derongan.minecraft.mineinabyss.API.Relic.Behaviour.*;
import com.derongan.minecraft.mineinabyss.plugin.AbyssContext;
import com.derongan.minecraft.mineinabyss.API.Relic.Relics.RelicType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RelicUseListener implements Listener {
    public final static Set<Material> passable = Stream.of(Material.AIR,
            Material.LEAVES,
            Material.LEAVES_2,
            Material.YELLOW_FLOWER,
            Material.VINE,
            Material.DIRT,
            Material.GRASS,
            Material.RED_ROSE,
            Material.SAPLING,
            Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM,
            Material.DOUBLE_PLANT).collect(Collectors.toSet());

    @EventHandler()
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
//        playerJoinEvent.getPlayer().setGravity(true);
//        playerJoinEvent.getPlayer().setWalkSpeed(.2f);
//        playerJoinEvent.getPlayer().setFlySpeed(.5f);
//        playerJoinEvent.getPlayer().setCompassTarget(new Location(playerJoinEvent.getPlayer().getWorld(), 0, -10000, 0));
    }

    @EventHandler()
    public void onPlayerFish(PlayerFishEvent playerFishEvent) {
    }

    @EventHandler()
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
    }

    @EventHandler()
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        RelicType relicType = RelicType.getRegisteredRelicType(player.getInventory().getItemInMainHand());

        if (relicType != null) {
            if (relicType.getBehaviour() instanceof InteractEntityBehaviour) {
                ((InteractEntityBehaviour) relicType.getBehaviour()).onInteractEntity(event);
            }
        }

    }

    @EventHandler()
    public void onEntityHit(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        Entity damager = entityDamageByEntityEvent.getDamager();

        if (damager instanceof Player) {
            Player player = (Player) damager;

            RelicType type = RelicType.getRegisteredRelicType(player.getInventory().getItemInMainHand());

            if (type != null) {
                if (type.getBehaviour() instanceof EntityHitRelicBehaviour) {
                    ((EntityHitRelicBehaviour) type.getBehaviour()).onHit(entityDamageByEntityEvent);
                } else {
                    entityDamageByEntityEvent.setCancelled(true);
                }
            }
        }

    }

    @EventHandler()
    public void onPlayerInteractEntity(PlayerInteractAtEntityEvent e) {
        RelicType relicType = ArmorStandBehaviour.registeredRelics.get(e.getRightClicked().getUniqueId());

        if(relicType != null){
            if(relicType.getBehaviour() instanceof ArmorStandBehaviour){
                ((ArmorStandBehaviour) relicType.getBehaviour()).onPlayerInteractEntity(e);
            }
        }
    }

    @EventHandler()
    public void onEntityDamageEvent(EntityDamageEvent e){
        if(e.getEntity() instanceof Player){
            for (ItemStack itemStack : ((Player) e.getEntity()).getInventory().getArmorContents()) {
                RelicType type = RelicType.getRegisteredRelicType(itemStack);
                if (type.getBehaviour() instanceof OnDamageRelicBehaviour) {
                    ((OnDamageRelicBehaviour) type.getBehaviour()).onDamage(e);
                }
            }
        }
    }

    @EventHandler()
    public void onPlayerUseItem(PlayerInteractEvent playerInteractEvent) {
        RelicType type = RelicType.getRegisteredRelicType(playerInteractEvent.getItem());

        if (type != null) {
            if (type.getBehaviour() instanceof UseRelicBehaviour) {
                ((UseRelicBehaviour) type.getBehaviour()).onUse(playerInteractEvent);
            } else {
                // Cancel events the relic shouldn't handle
                playerInteractEvent.setCancelled(true);
            }
        }

        if (playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block block = playerInteractEvent.getClickedBlock();

            CleanUpWorldRelicBehaviour.cleanUp(block.getLocation());
        }
    }

    @EventHandler()
    public void onPlayerChat(AsyncPlayerChatEvent chatEvent) {

        Player player = chatEvent.getPlayer();
        RelicType type = RelicType.getRegisteredRelicType(player.getInventory().getItemInMainHand());

        if (type != null) {
            if (type.getBehaviour() instanceof ChatRelicBehaviour) {
                ((ChatRelicBehaviour) type.getBehaviour()).onChat(chatEvent);
            }
        }
    }
}

