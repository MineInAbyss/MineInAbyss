package com.derongan.minecraft.mineinabyss.World;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class SectionUtils {

    /**
     * Get the location on another layer that is the same abyss space as the initial location.
     * The two sections must be next to eachother
     * @param fromSection the section the initial location is on
     * @param toSection the section we are translating the point to
     * @param initialLocation The initial location
     * @return A new location that corresponds to the original location
     */
    public static Location getCorrespondingPoint(Section fromSection, Section toSection, Location initialLocation){
        Validate.isTrue(Math.abs(fromSection.getIndex()-toSection.getIndex()) == 1, "Sections must be adjacent");

        Location fromSectionLoc;
        Location toSectionLoc;

        // We decide which two points we are translating between.
        if(fromSection.getIndex() < toSection.getIndex()){
            fromSectionLoc = fromSection.getReferenceLocationBottom();
            toSectionLoc = toSection.getReferenceLocationTop();
        } else {
            fromSectionLoc = fromSection.getReferenceLocationTop();
            toSectionLoc = toSection.getReferenceLocationBottom();
        }

        // fromX + n = toX
        // toX - fromX = n
        Vector delta = toSectionLoc.toVector().subtract(fromSectionLoc.toVector());

        return initialLocation.clone().add(delta);
    }
}
