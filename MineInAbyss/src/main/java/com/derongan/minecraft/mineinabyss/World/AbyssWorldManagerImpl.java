package com.derongan.minecraft.mineinabyss.World;

import com.derongan.minecraft.mineinabyss.Ascension.Effect.AscensionEffectBuilder;
import com.derongan.minecraft.mineinabyss.Ascension.Effect.Configuration.EffectConfiguror;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public class AbyssWorldManagerImpl implements AbyssWorldManager {
    private List<Section> sections;
    private List<Layer> layers;

    private int numLayers;
    private int numSections;

    private static final String LAYER_KEY = "layers";
    private static final String NAME_KEY = "name";
    private static final String SUB_KEY = "sub";
    private static final String SECTION_KEY = "sections";
    private static final String EFFECTS_KEY = "effects";

    public AbyssWorldManagerImpl(Configuration config) {
        layers = new ArrayList<>();
        sections = new ArrayList<>();

        List<Map<?, ?>> layerlist = config.getMapList(LAYER_KEY);

        layerlist.forEach(this::parseLayer);
    }

    private Layer parseLayer(Map<?, ?> map) {
        String layerName = (String) map.get(NAME_KEY);
        String subHeader = (String) map.get(SUB_KEY);

        LayerImpl layer = new LayerImpl(layerName, subHeader, numLayers++);
        layers.add(layer);

        List<Map<?, ?>> sectionMap = (List<Map<?, ?>>) map.get(SECTION_KEY);

        layer.setSections(
                sectionMap
                        .stream()
                        .map(a -> parseSection(a, layer))
                        .collect(Collectors.toList())
        );

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

    private Section parseSection(Map<?, ?> map, Layer layer) {
        String worldName = (String) map.get("world");
        World world = Bukkit.getWorld(worldName);

        Location refBottom = parseLocation((List<Integer>) map.get("refBottom"), world);
        Location refTop = parseLocation((List<Integer>) map.get("refTop"), world);

        SectionImpl section = new SectionImpl(numSections++, world, layer, refTop, refBottom);

        sections.add(section);

        return section;
    }

    private Location parseLocation(List<Integer> points, World world) {
        return new Location(world, points.get(0), points.get(1), points.get(2));
    }

    @Override
    public List<Layer> getLayers() {
        return Collections.unmodifiableList(layers);
    }

    @Override
    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    @Override
    public Section getSectonAt(int index) {
        if (index >= sections.size() || index < 0)
            return null;

        return sections.get(index);
    }

    @Override
    public Layer getLayerAt(int index) {
        if (index >= layers.size() || index < 0)
            return null;

        return layers.get(index);
    }
}
