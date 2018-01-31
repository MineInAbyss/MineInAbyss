package com.derongan.minecraft.mineinabyss.Relic;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.ChatRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.CleanUpWorldRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.EntityHitRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.UseRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
import net.minecraft.server.v1_12_R1.Item;
import net.minecraft.server.v1_12_R1.Items;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RelicUseListener implements Listener {
    AbyssContext context;

    public static Set<Material> passable = Stream.of(Material.AIR,
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
    public void onEntityHit(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        Entity damager = entityDamageByEntityEvent.getDamager();

        if (damager instanceof Player) {
            Player player = (Player) damager;

            RelicType type = RelicType.getRegisteredRelicType(((Player) damager).getInventory().getItemInMainHand());

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
    public void onPlayerInteractEntity(PlayerInteractAtEntityEvent e){
        Player p = e.getPlayer();

        if(e.getRightClicked().getType().equals(EntityType.ARMOR_STAND)) {
            if (e.getRightClicked().getScoreboardTags().contains("Campfire")) {
                ArmorStand as = (ArmorStand) e.getRightClicked();
                if (p.getInventory().getItemInMainHand().getType().equals(Material.COOKED_BEEF)) {
                    as.setItemInHand(p.getInventory().getItemInMainHand());
                    p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
                    e.setCancelled(true);
                    return;
                }else if (!as.getItemInHand().getType().equals(Material.AIR)) {
                    ItemStack is = as.getItemInHand();
                    is.setAmount(1);
                    p.getInventory().setItemInMainHand(is);
                    as.setItemInHand(new ItemStack(Material.AIR));
                    e.setCancelled(true);
                    return;
                }/*else if (as.getItemInHand() == null) {//p.getInventory().getItemInMainHand().getType() == null &&
                    p.getInventory().addItem(as.getHelmet());
                    as.remove();
                    e.setCancelled(true);
                }*/
            }

            ArmorStand as = (ArmorStand) e.getRightClicked();
            p.getInventory().addItem(as.getHelmet());
            as.remove();
            e.setCancelled(true);
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

        if(playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            Block block = playerInteractEvent.getClickedBlock();

            CleanUpWorldRelicBehaviour.cleanUp(block.getLocation());
        }
    }

    //@EventHandler()
    //public void pickedUpRelic(PlayerArmorStandManipulateEvent e){
    //    ArmorStand clickedEntity = e.getRightClicked();
    //    if(clickedEntity.getCustomName() == "Relic"){
    //    clickedEntity.remove();
    //    }
    //}

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

