package com.derongan.minecraft.mineinabyss.plugin.Layer.Configuration;

import com.derongan.minecraft.mineinabyss.plugin.AbyssContext;
import com.derongan.minecraft.mineinabyss.plugin.Ascension.Effect.AscensionEffectBuilder;
import com.derongan.minecraft.mineinabyss.plugin.Ascension.Effect.Configuration.EffectConfiguror;
import com.derongan.minecraft.mineinabyss.plugin.Layer.Layer;
import com.derongan.minecraft.mineinabyss.plugin.Layer.Section;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class LayerConfiguror {
    private FileConfiguration configuration;
    private AbyssContext context;

    public LayerConfiguror(FileConfiguration configuration, AbyssContext context) {
        this.configuration = configuration;
        this.context = context;
    }


    public Map<String, Layer> loadLayers() {
        EffectConfiguror effectConfiguror = new EffectConfiguror(configuration, context);

        Map<String, Layer> layerMap = new HashMap<>();
        Layer prev = null;

        for (Map layerData : configuration.getMapList("layers")) {
            Layer layer = buildLayer(layerData);

            layer.setEffectsOnLayer(
                    effectConfiguror.getEffectsFromMap((Collection<Map>) layerData.get("effects"))
            );

            layer.setPrevLayer(prev);
            layer.setDeathMessage((String) layerData.getOrDefault("abyssDeathMessage", null));
            layer.setOffset((int) layerData.getOrDefault("offset", 50));

            if (prev != null) {
                prev.setNextLayer(layer);
            }
            prev = layer;

            layerMap.put(layer.getName(), layer);
        }
        return layerMap;
    }

    private Layer buildLayer(Map layerData) {
        Layer layer = new Layer((String) layerData.get("name"), context);
        World world = context.getPlugin().getServer().getWorld(layer.getName());

        layer.setDeathMessage((String) layerData.getOrDefault("abyssDeathMessage", null));
        layer.setOffset((int) layerData.getOrDefault("offset", 50));

        layer.setSections(buildSections((List<List<Integer>>) layerData.get("sectionOffsets"), world));
        return layer;
    }

    private List<Section> buildSections(List<List<Integer>> sectionData, World world) {
        if (sectionData == null) {
            return Collections.emptyList();
        }

        return sectionData.stream().map(a -> {
            Integer x = a.get(0);
            Integer z = a.get(1);
            Integer sharedWithBelow = a.get(2);
            return new Section(new Vector(x, 0, z), sharedWithBelow, world);
        }).collect(Collectors.toList());
    }
}
