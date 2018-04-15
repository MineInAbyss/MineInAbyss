package com.derongan.minecraft.mineinabyss.World;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import static com.derongan.minecraft.mineinabyss.World.MinecraftConstants.WORLD_HEIGHT;

public class SectionUtils {

    /**
     * Get the location on another layer that is the same abyss space as the initial location.
     * The two sections must be next to eachother
     * @param fromSection the section the initial location is on
     * @param toSection the section we are translating the point to
     * @param initialLocation The initial location
     * @return A new location that corresponds to the original location
     */
    public static Location getCorrespondingLocation(Section fromSection, Section toSection, Location initialLocation){
        validateSectionsAdjacent(fromSection, toSection);

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

        Location newLoc = initialLocation.clone().add(delta);
        newLoc.setWorld(toSection.getWorld());
        return newLoc;
    }

    public static int getSharedBlocks(Section sectionA, Section sectionB){
        validateSectionsAdjacent(sectionA, sectionB);

        Location locA;
        Location locB;

        // We decide which two points we are translating between.
        if(sectionA.getIndex() < sectionB.getIndex()){
            locA = sectionA.getReferenceLocationBottom();
            locB = sectionB.getReferenceLocationTop();
        } else {
            locA = sectionA.getReferenceLocationTop();
            locB = sectionB.getReferenceLocationBottom();
        }

        int yA = locA.getBlockY();
        int yB = locB.getBlockY();

        return WORLD_HEIGHT - Math.max(yA, yB) + Math.min(yA, yB);
    }

    private static void validateSectionsAdjacent(Section fromSection, Section toSection) {
        Validate.isTrue(Math.abs(fromSection.getIndex()-toSection.getIndex()) == 1, "Sections must be adjacent");
    }
}
