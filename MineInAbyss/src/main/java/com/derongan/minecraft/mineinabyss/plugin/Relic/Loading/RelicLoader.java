package com.derongan.minecraft.mineinabyss.plugin.Relic.Loading;

import com.derongan.minecraft.mineinabyss.plugin.AbyssContext;
import com.derongan.minecraft.mineinabyss.API.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Relics.StandardRelicType;
import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.Relic.Relics.StandardRelicType;
import com.derongan.minecraft.mineinabyss.Relic.Relics.SlightlyOffzRelicType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.net.URI;
import java.util.Collection;

public class RelicLoader {
    private static RelicClassLoader relicClassLoader;

    //TODO do we need the full context here? No. Do we want to inject this? Probably. Do I care? Yes... :(
    public static void loadAllRelics(AbyssContext context) {
        relicClassLoader = new RelicClassLoader(context);
        for (StandardRelicType standardRelicType : StandardRelicType.values()) {
            RelicType.registerRelicType(standardRelicType);
        }
        for (SlightlyOffzRelicType slightlyOffzRelicType : SlightlyOffzRelicType.values()) {
            RelicType.registerRelicType(slightlyOffzRelicType);
        }

        String relicRelDir = (String) context.getConfig().get("storage.relicpath");
        URI fullURI = context.getPlugin().getDataFolder().toURI().resolve(relicRelDir);

        Collection<File> files = FileUtils.listFiles(
                new File(fullURI),
                new RegexFileFilter(".*\\.class$"),
                TrueFileFilter.INSTANCE
        );

        files.forEach(a -> {
            String relPath = fullURI.relativize(a.toURI()).toString();
            String className = relPath.substring(0, relPath.length() - 6).replace("/", ".");

            // We just need to load the enums
            try {
                Class<?> someClass = relicClassLoader.loadClass(className);
                if(someClass.isEnum() &&  RelicType.class.isAssignableFrom(someClass)){
                    someClass.getEnumConstants();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    public static void unloadAllRelics() {
        // Unregister
        RelicType.unregisterAllRelics();

        // Destroy
        relicClassLoader = null;
    }
}
