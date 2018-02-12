package com.derongan.minecraft.mineinabyss.plugin.Relic.Loading;

import com.derongan.minecraft.mineinabyss.plugin.AbyssContext;
import com.derongan.minecraft.mineinabyss.API.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Relics.SlightlyOffzRelicType;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Relics.StandardRelicType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class RelicLoader {
    private static RelicClassLoader relicClassLoader;

    //TODO do we need the full context here? No. Do we want to inject this? Probably. Do I care? Yes... :(
    public static void loadAllRelics(AbyssContext context) {
        for (StandardRelicType standardRelicType : StandardRelicType.values()) {
            RelicType.registerRelicType(standardRelicType);
        }
//        for (SlightlyOffzRelicType slightlyOffzRelicType : SlightlyOffzRelicType.values()) {
//            RelicType.registerRelicType(slightlyOffzRelicType);
//        }

        String relicRelDir = (String) context.getConfig().get("storage.relicpath");
        URI fullURI = context.getPlugin().getDataFolder().toURI().resolve(relicRelDir);

        Collection<File> files = FileUtils.listFiles(
                new File(fullURI),
                new RegexFileFilter(".*\\.jar$"),
                TrueFileFilter.INSTANCE
        );

        URL[] urls = files.stream().map(a -> {
            try {
                return a.toURI().toURL();
            } catch (MalformedURLException e) {
                return null;
            }
        }).filter(Objects::nonNull).toArray(URL[]::new);

        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                relicClassLoader = new RelicClassLoader(urls, context);
                return null;
            }
        });


        files.forEach(a -> {
            try {
                JarFile jarFile = new JarFile(a);
                Enumeration<JarEntry> e = jarFile.entries();

                while (e.hasMoreElements()) {
                    JarEntry je = e.nextElement();
                    if (je.isDirectory() || !je.getName().endsWith(".class")) {
                        continue;
                    }
                    // -6 because of .class
                    String className = je.getName().substring(0, je.getName().length() - 6);
                    className = className.replace('/', '.');
                    Class c = relicClassLoader.loadClass(className);

                    if (c.isEnum() && RelicType.class.isAssignableFrom(c)) {
                        c.getEnumConstants();
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
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
