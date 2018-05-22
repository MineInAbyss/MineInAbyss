package com.derongan.minecraft.mineinabyss.World;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Configuration.ConfigurationConstants;
import com.derongan.minecraft.mineinabyss.MineInAbyss;
import com.google.common.annotations.VisibleForTesting;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class WorldDataConfigManager {
    private AbyssContext context;
    private static final String ENTITIES_KEY = "entities";

    public WorldDataConfigManager(AbyssContext context) {
        this.context = context;
    }

    public Collection<ChunkEntity> loadChunkData(Chunk chunk){
        Path path = getChunkDataPath(chunk);

        if(path.toFile().exists()){
            YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
            return (Collection<ChunkEntity>) config.getList(ENTITIES_KEY);
        } else {
            return new ArrayList<>();
        }
    }

    public void saveChunkData(Chunk chunk, Collection<ChunkEntity> entities) throws IOException {
        Path path = getChunkDataPath(chunk);

        // Recreate directories if missing
        path.toFile().getParentFile().mkdirs();

        YamlConfiguration config = new YamlConfiguration();
        config.set(ENTITIES_KEY, entities);

        config.save(path.toFile());
    }

    @VisibleForTesting
    Path getChunkDataPath(Chunk chunk) {
        String worldDir = chunk.getWorld().getName();
        int x = chunk.getX();
        int z = chunk.getZ();

        return MineInAbyss.getInstance().getDataFolder()
                .toPath()
                .resolve(ConfigurationConstants.WORLD_ENTITY_DATA_DIR)
                .resolve(worldDir)
                .resolve(String.format("%d_%d.yml", x, z));
    }
}
