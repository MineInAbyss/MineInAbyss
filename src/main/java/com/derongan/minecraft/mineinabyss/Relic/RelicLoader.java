package com.derongan.minecraft.mineinabyss.Relic;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.Relic.Relics.StandardRelicType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

public class RelicLoader {
    private static RelicClassLoader relicClassLoader;

    //TODO do we need the full context here? No. Do we want to inject this? Probably. Do I care? Yes... :(
    public static void loadAllRelics(AbyssContext context) {
        relicClassLoader = new RelicClassLoader(context);
        for (StandardRelicType standardRelicType : StandardRelicType.values()) {
            RelicType.registerRelicType(standardRelicType);
        }

        String relicRelDir = (String) context.getConfig().get("storage.relicpath");
        URI fullURI = context.getPlugin().getDataFolder().toURI().resolve(relicRelDir);

        Collection<File> files = FileUtils.listFiles(
                new File(fullURI),
                new RegexFileFilter(".*\\.class$"),
                TrueFileFilter.INSTANCE
        );

        files.forEach(a->{
            String relPath = fullURI.relativize(a.toURI()).toString();
            String className = relPath.substring(0,relPath.length()-6).replace("/", ".");

            try {
                Class<?> someClass = relicClassLoader.loadClass(className);
                if(Arrays.asList(someClass.getInterfaces()).contains(RelicType.class)) {
                    RelicType.registerRelicType((RelicType) someClass.newInstance());
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e){
                System.out.println("Could not load " + className);
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
