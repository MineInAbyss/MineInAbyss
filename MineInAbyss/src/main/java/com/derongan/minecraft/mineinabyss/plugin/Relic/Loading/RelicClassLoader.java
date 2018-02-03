package com.derongan.minecraft.mineinabyss.plugin.Relic.Loading;

import com.derongan.minecraft.mineinabyss.plugin.AbyssContext;
import com.derongan.minecraft.mineinabyss.plugin.MineInAbyss;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.stream.Collectors;

public class RelicClassLoader extends URLClassLoader {
    AbyssContext context;

    protected RelicClassLoader(URL[] urls, AbyssContext context) {
        super(urls, MineInAbyss.class.getClassLoader());
    }
}
