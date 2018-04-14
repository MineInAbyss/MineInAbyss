package com.derongan.minecraft.mineinabyss.Player;

import com.derongan.minecraft.mineinabyss.Configuration.ConfigurationConstants;
import com.derongan.minecraft.mineinabyss.MineInAbyss;
import com.google.common.annotations.VisibleForTesting;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public class PlayerDataManager {
    private static final String UUID_KEY = "uuid";
    private static final String LAYER_KEY = "layer";
    private static final String SECTION_KEY = "section";
    private static final String AFFECTABLE_KEY = "affectable";
    private static final String ANCHORED_KEY = "anchored";

    public static PlayerData loadPlayerData(Player player) {
        Path path = getPlayerDataPath(player);

        if(path.toFile().exists()){
            YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
            PlayerData data = new PlayerDataImpl(player);
            data.setAffectedByCurse(config.getBoolean(AFFECTABLE_KEY));
            data.setAnchored(config.getBoolean(ANCHORED_KEY));

            return data;
        } else {
            PlayerData data = new PlayerDataImpl(player);

            return data;
        }
    }

    public static void savePlayerData(PlayerData playerData) throws IOException {
        Path path = getPlayerDataPath(playerData.getPlayer());

        // Recreate directories if missing
        path.toFile().getParentFile().mkdirs();

        YamlConfiguration config = new YamlConfiguration();
        config.set(UUID_KEY, playerData.getPlayer().getUniqueId().toString());
//        config.set(LAYER_KEY, playerData.getCurrentLayer().getIndex());
//        config.set(SECTION_KEY, playerData.getCurrentSection().getIndex());
        config.set(AFFECTABLE_KEY, playerData.isAffectedByCurse());
        config.set(ANCHORED_KEY, playerData.isAnchored());

        config.save(path.toFile());
    }

    @VisibleForTesting
    static Path getPlayerDataPath(Player player) {
        UUID uuid = player.getUniqueId();
        return MineInAbyss.getInstance().getDataFolder()
                .toPath()
                .resolve(ConfigurationConstants.PLAYER_DATA_DIR)
                .resolve(uuid.toString() + ".yml");
    }
}
