package com.derongan.minecraft.mineinabyss.relic;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.relic.behaviour.*;
import com.derongan.minecraft.mineinabyss.relic.relics.RelicType;
import com.derongan.minecraft.mineinabyss.world.AbyssWorldManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
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
            Material.VINE,
            Material.DIRT,
            Material.GRASS,
            Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM,
            Material.TALL_GRASS).collect(Collectors.toSet());

    private AbyssContext context;
    private AbyssWorldManager worldManager;

    public RelicUseListener(AbyssContext context) {
        this.context = context;
        worldManager = context.getWorldManager();
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

        if(!worldManager.isAbyssWorld(event.getPlayer().getWorld())){
            return;
        }

        RelicType relicType = RelicType.getRegisteredRelicType(player.getInventory().getItemInMainHand());

        if (relicType != null) {
            if (relicType.getBehaviour() instanceof InteractEntityBehaviour) {
                ((InteractEntityBehaviour) relicType.getBehaviour()).onInteractEntity(event);
            }
        }

    }

    @EventHandler()
    public void onEntityHit(EntityDamageByEntityEvent entityDamageByEntityEvent) {

        if(!worldManager.isAbyssWorld(entityDamageByEntityEvent.getDamager().getWorld())){
            return;
        }

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
        if(!worldManager.isAbyssWorld(e.getPlayer().getWorld())){
            return;
        }

        RelicType relicType = ArmorStandBehaviour.registeredRelics.get(e.getRightClicked().getUniqueId());

        if(relicType != null){
            if(relicType.getBehaviour() instanceof ArmorStandBehaviour){
                ((ArmorStandBehaviour) relicType.getBehaviour()).onPlayerInteractEntity(e);
            }
        }
    }

    @EventHandler()
    public void onEntityDamageEvent(EntityDamageEvent e){
        if(!worldManager.isAbyssWorld(e.getEntity().getWorld())){
            return;
        }

        if(e.getEntity() instanceof Player){
            for (ItemStack itemStack : ((Player) e.getEntity()).getInventory().getArmorContents()) {
                RelicType type = RelicType.getRegisteredRelicType(itemStack);
                if (type != null && type.getBehaviour() instanceof OnDamageRelicBehaviour) {
                    ((OnDamageRelicBehaviour) type.getBehaviour()).onDamage(e);
                }
            }
        }
    }

    @EventHandler()
    public void onPlayerUseItem(PlayerInteractEvent playerInteractEvent) {
        if(!worldManager.isAbyssWorld(playerInteractEvent.getPlayer().getWorld())){
            return;
        }

        RelicType type = RelicType.getRegisteredRelicType(playerInteractEvent.getItem());

        if (type != null) {
            if (type.getBehaviour() instanceof UseRelicBehaviour) {
                ((UseRelicBehaviour) type.getBehaviour()).onUse(playerInteractEvent);
            } else {
                // Cancel events the relic shouldn't handle
				if (!(type.getBehaviour() instanceof ConsumeRelicBehaviour)) { //let eat events continue
					playerInteractEvent.setCancelled(true);
				}
            }
        }

        if (playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block block = playerInteractEvent.getClickedBlock();

            CleanUpWorldRelicBehaviour.cleanUp(block.getLocation());
        }
    }

    @EventHandler()
    public void onPlayerChat(AsyncPlayerChatEvent chatEvent) {
        if(!worldManager.isAbyssWorld(chatEvent.getPlayer().getWorld())){
            return;
        }

        Player player = chatEvent.getPlayer();
        RelicType type = RelicType.getRegisteredRelicType(player.getInventory().getItemInMainHand());

        if (type != null) {
            if (type.getBehaviour() instanceof ChatRelicBehaviour) {
                ((ChatRelicBehaviour) type.getBehaviour()).onChat(chatEvent);
            }
        }
    }

    @EventHandler()
    public void onPlayerConsumeItem(PlayerItemConsumeEvent e) {
        if(!worldManager.isAbyssWorld(e.getPlayer().getWorld())){
            return;
        }

        RelicType type = RelicType.getRegisteredRelicType(e.getItem());

        if (type != null) {
            if (type.getBehaviour() instanceof ConsumeRelicBehaviour) {
                ((ConsumeRelicBehaviour) type.getBehaviour()).onConsume(e);
            }
        }
    }
}

