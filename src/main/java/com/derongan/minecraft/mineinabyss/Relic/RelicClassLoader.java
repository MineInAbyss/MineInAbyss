package com.derongan.minecraft.mineinabyss.Relic;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.MineInAbyss;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

public class RelicClassLoader extends URLClassLoader {
    AbyssContext context;

    protected RelicClassLoader(AbyssContext context) {
        super(getUrls(context), MineInAbyss.class.getClassLoader());
    }

    private static URL[] getUrls(AbyssContext context) {
        String relicRelDir = (String) context.getConfig().get("storage.relicpath");
        URI fullURI = context.getPlugin().getDataFolder().toURI().resolve(relicRelDir);

        URL[] urls;
        try {
            urls = new URL[]{fullURI.toURL()};
        } catch (MalformedURLException e) {
            urls = new URL[]{};
        }

        return urls;
    }
}
