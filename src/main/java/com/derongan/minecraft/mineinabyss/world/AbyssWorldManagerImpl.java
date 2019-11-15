package com.derongan.minecraft.mineinabyss.world;

import com.derongan.minecraft.deeperworld.world.WorldManager;
import com.derongan.minecraft.deeperworld.world.section.Section;
import com.derongan.minecraft.mineinabyss.ascension.effect.AscensionEffectBuilder;
import com.derongan.minecraft.mineinabyss.ascension.effect.configuration.EffectConfiguror;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;

import java.util.*;
import java.util.stream.Collectors;

public class AbyssWorldManagerImpl implements AbyssWorldManager {
    private static final String LAYER_KEY = "layers";
    private static final String NAME_KEY = "name";
    private static final String SUB_KEY = "sub";
    private static final String SECTION_KEY = "sections";
    private static final String EFFECTS_KEY = "effects";
    private List<Layer> layers;
    private Set<World> abyssWorlds;
    private int numLayers;


    public AbyssWorldManagerImpl(Configuration config) {
        layers = new ArrayList<>();

        abyssWorlds = new HashSet<>();

        List<Map<?, ?>> layerlist = config.getMapList(LAYER_KEY);

        layerlist.forEach(this::parseLayer);
    }

    private Layer parseLayer(Map<?, ?> map) {
        String layerName = (String) map.get(NAME_KEY);
        String subHeader = (String) map.get(SUB_KEY);

        LayerImpl layer = new LayerImpl(layerName, subHeader, numLayers++);
        layers.add(layer);

        WorldManager worldManager = Bukkit.getServicesManager().load(WorldManager.class);

        List<Section> sections = ((List<String>) map.get(SECTION_KEY)).stream().map(worldManager::getSectionFor).collect(Collectors.toList());

        layer.setSections(sections);

        sections.forEach(a -> abyssWorlds.add(a.getWorld()));

        List<Map<?, ?>> effectMap = (List<Map<?, ?>>) map.get(EFFECTS_KEY);
        if (effectMap == null)
            effectMap = Collections.emptyList();

        layer.setEffects(
                effectMap
                        .stream()
                        .map(this::parseAscensionEffects)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );

        return layer;
    }

    private AscensionEffectBuilder parseAscensionEffects(Map<?, ?> map) {
        return EffectConfiguror.createBuilderFromMap(map);
    }

    @Override
    public List<Layer> getLayers() {
        return ImmutableList.copyOf(layers);
    }

    @Override
    public Layer getLayerForSection(Section section) {
        return layers.stream().filter(a -> a.containsSection(section)).findFirst().orElse(null);
    }

    @Override
    public boolean isAbyssWorld(World worldName) {
        return abyssWorlds.contains(worldName);
    }
}
