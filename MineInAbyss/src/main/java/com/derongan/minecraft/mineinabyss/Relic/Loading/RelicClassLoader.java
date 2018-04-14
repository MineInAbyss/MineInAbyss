package com.derongan.minecraft.mineinabyss.Relic.Loading;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.MineInAbyss;

import java.net.URL;
import java.net.URLClassLoader;

public class RelicClassLoader extends URLClassLoader {
    AbyssContext context;

    protected RelicClassLoader(URL[] urls, AbyssContext context) {
        super(urls, MineInAbyss.class.getClassLoader());
    }
}
