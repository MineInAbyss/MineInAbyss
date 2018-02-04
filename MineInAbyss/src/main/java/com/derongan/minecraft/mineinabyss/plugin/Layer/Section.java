package com.derongan.minecraft.mineinabyss.plugin.Layer;

import org.bukkit.World;
import org.bukkit.util.Vector;

public class Section {
    private final Vector offset;
    private final int sharedWithBelow;
    private final World world;

    public Section(Vector offset, int sharedWithBelow, World world) {
        this.offset = offset;
        this.sharedWithBelow = sharedWithBelow;
        this.world = world;
    }

    public Vector getOffset() {
        return offset;
    }

    public int getSharedWithBelow() {
        return sharedWithBelow;
    }

    public World getWorld() {
        return world;
    }
}
