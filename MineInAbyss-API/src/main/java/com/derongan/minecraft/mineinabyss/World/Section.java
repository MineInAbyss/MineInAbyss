package com.derongan.minecraft.mineinabyss.World;

import org.bukkit.Location;

public interface Section {
    /**
     * Gets the index of this Section. Higher sections have lower index
     * @return The index of this section
     */
    int getIndex();

    /**
     * Gets the reference location between this section and the one above it.
     *
     * This method and the section above's {@link Section#getReferenceLocationBottom()}
     * represent the same location in the abyss.
     * @return The top reference point
     */
    Location getReferenceLocationTop();

    /**
     * Gets the reference location between this section and the one below it.
     *
     * This method and the section below's {@link Section#getReferenceLocationTop()} ()}
     * represent the same location in the abyss.
     * @return The bottom reference point
     */
    Location getReferenceLocationBottom();
}
