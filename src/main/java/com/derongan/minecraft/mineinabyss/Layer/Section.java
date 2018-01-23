package com.derongan.minecraft.mineinabyss.Layer;

import org.bukkit.util.Vector;

public class Section {
    private final Vector offset;
    private final int sharedWithBelow;

    public Section(Vector offset, int sharedWithBelow) {
        this.offset = offset;
        this.sharedWithBelow = sharedWithBelow;
    }

    public Vector getOffset() {
        return offset;
    }

    public int getSharedWithBelow() {
        return sharedWithBelow;
    }
}
