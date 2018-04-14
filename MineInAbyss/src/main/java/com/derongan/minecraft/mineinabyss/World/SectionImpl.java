package com.derongan.minecraft.mineinabyss.World;


import org.bukkit.Location;

public class SectionImpl implements Section {
    private int index;

    private Location referenceTop;
    private Location referenceBottom;

    @Override
    public Location getReferenceLocationTop() {
        return referenceTop;
    }

    @Override
    public Location getReferenceLocationBottom() {
        return referenceBottom;
    }

    @Override
    public int getIndex() {
        return index;
    }
}
