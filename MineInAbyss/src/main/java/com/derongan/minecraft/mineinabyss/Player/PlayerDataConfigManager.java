package com.derongan.minecraft.mineinabyss.Player;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Configuration.ConfigurationConstants;
import com.derongan.minecraft.mineinabyss.MineInAbyss;
import com.derongan.minecraft.mineinabyss.World.AbyssWorldManager;
import com.google.common.annotations.VisibleForTesting;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public class PlayerDataConfigManager {
    private static final String UUID_KEY = "uuid";
    private static final String LAYER_KEY = "layer";
    private static final String SECTION_KEY = "section";
    private static final String AFFECTABLE_KEY = "affectable";
    private static final String ANCHORED_KEY = "anchored";

    private AbyssContext context;

    public PlayerDataConfigManager(AbyssContext context) {
        this.context = context;
    }

    public PlayerData loadPlayerData(Player player) {
        Path path = getPlayerDataPath(player);

        AbyssWorldManager manager = context.getWorldManager();

        if(path.toFile().exists()){
            YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
            PlayerData data = new PlayerDataImpl(player);
            data.setAffectedByCurse(config.getBoolean(AFFECTABLE_KEY));
            data.setAnchored(config.getBoolean(ANCHORED_KEY));
            data.setCurrentLayer(manager.getLayerAt(config.getInt(LAYER_KEY)));
            data.setCurrentSection(manager.getSectonAt(config.getInt(SECTION_KEY)));

            return data;
        } else {
            PlayerData data = new PlayerDataImpl(player);
            data.setCurrentLayer(manager.getLayerAt(0));
            data.setCurrentSection(manager.getSectonAt(0));

            return data;
        }
    }

    public void savePlayerData(PlayerData playerData) throws IOException {
        Path path = getPlayerDataPath(playerData.getPlayer());

        // Recreate directories if missing
        path.toFile().getParentFile().mkdirs();

        YamlConfiguration config = new YamlConfiguration();
        config.set(UUID_KEY, playerData.getPlayer().getUniqueId().toString());
        config.set(LAYER_KEY, playerData.getCurrentLayer().getIndex());
        config.set(SECTION_KEY, playerData.getCurrentSection().getIndex());
        config.set(AFFECTABLE_KEY, playerData.isAffectedByCurse());
        config.set(ANCHORED_KEY, playerData.isAnchored());

        config.save(path.toFile());
    }

    @VisibleForTesting
    Path getPlayerDataPath(Player player) {
        UUID uuid = player.getUniqueId();
        return MineInAbyss.getInstance().getDataFolder()
                .toPath()
                .resolve(ConfigurationConstants.PLAYER_DATA_DIR)
                .resolve(uuid.toString() + ".yml");
    }
}
