package com.derongan.minecraft.mineinabyss.Relic;

import com.derongan.minecraft.mineinabyss.Relic.Behaviour.ArmorStandBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Behaviour.Behaviours.LootableRelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.Relics.LootableRelicType;
import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.World.ChunkEntityImpl;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents an entity that invisibly holds a relic
 */
public class RelicGroundEntity extends ChunkEntityImpl {
    public static final String NAME_KEY = "name";
    public static final String LORE_KEY = "lore";
    public static final String MAT_KEY = "material";
    public static final String DURABILITY_KEY = "durability";
    public static final String RARITY_KEY = "rarity";

    private static LootableRelicBehaviour behaviour = new LootableRelicBehaviour();
    private static RelicType lootableType = new LootableRelicType();

    private String name;
    private String lore;
    private String mat;
    private short dur;
    private String rarity;

    public RelicGroundEntity(String name, String lore, String mat, short dur, String rarity, int x, int y, int z) {
        super(-1, x, y, z);
        this.name = name;
        this.lore = lore;
        this.mat = mat;
        this.dur = dur;
        this.rarity = rarity;
    }

    public RelicGroundEntity(RelicType relicType, int x, int y, int z) {
        super(-1, x, y, z);

        this.name = relicType.getName();
        this.lore = relicType.getLore().stream().collect(Collectors.joining("\n"));
        this.mat = relicType.getMaterial().name();
        this.dur = relicType.getDurability();
        this.rarity = relicType.getRarity().name();
    }

    //Deserialization constructor
    public RelicGroundEntity(Map<String, Object> serializedMap) {
        super(((Number) serializedMap.get(TIME_REMAINING_KEY)).longValue(),
                ((Number) serializedMap.get(X_KEY)).intValue(),
                ((Number) serializedMap.get(Y_KEY)).intValue(),
                ((Number) serializedMap.get(Z_KEY)).intValue());

        name = (String) serializedMap.get(NAME_KEY);
        lore = (String) serializedMap.get(LORE_KEY);
        mat = (String) serializedMap.get(MAT_KEY);
        dur = (short) ((Number) serializedMap.get(DURABILITY_KEY)).shortValue();
        rarity = (String) serializedMap.get(RARITY_KEY);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serializedMap = new HashMap<>();

        serializedMap.put(TIME_REMAINING_KEY, getTimeRemaining());
        serializedMap.put(TIME_SERIALIZED_KEY, getCurrentTime());

        serializedMap.put(NAME_KEY, name);
        serializedMap.put(LORE_KEY, lore);
        serializedMap.put(MAT_KEY, mat);
        serializedMap.put(DURABILITY_KEY, dur);
        serializedMap.put(RARITY_KEY, rarity);

        serializedMap.put(X_KEY, getX());
        serializedMap.put(Y_KEY, getY());
        serializedMap.put(Z_KEY, getZ());

        return serializedMap;
    }

    @Override
    public Entity makeEntity(Location loc) {
        ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc.add(.5, -1.4, .5).setDirection(new Vector(0, 0, 0)), EntityType.ARMOR_STAND);
        as.setVisible(false);
        as.setCollidable(false);
        as.setRightArmPose(new EulerAngle(-Math.PI / 2, -Math.PI / 2, 0));
        as.setInvulnerable(true);
        as.setGravity(false);

        ItemStack item = new org.bukkit.inventory.ItemStack(Material.valueOf(mat), 1, dur);

        ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(true);
        meta.setLore(Arrays.asList(lore.split("\n")));

        meta.setDisplayName(RelicRarity.valueOf(rarity).getColor() + name);


        item.setItemMeta(meta);

        as.setItemInHand(item);

        behaviour.registerRelic(as.getUniqueId(), lootableType);

        return as;
    }
}
